#!/bin/bash
# PostToolUse(Edit|Write) 훅: frontend/src 아래 .ts/.tsx 파일을 고친 뒤 oxlint --fix로 자동 수정한다.
# 자동 수정이 실패해도 Claude 작업이 멈추지 않도록 항상 exit 0으로 끝낸다.

file_path=$(jq -r '.tool_input.file_path // empty')
project_dir="${CLAUDE_PROJECT_DIR:-$(cd "$(dirname "$0")/../.." && pwd)}"
frontend_dir="$project_dir/frontend"

case "$file_path" in
  "$frontend_dir"/src/*.ts|"$frontend_dir"/src/*.tsx) ;;
  *) exit 0 ;;
esac

cd "$frontend_dir" && ./node_modules/.bin/oxlint --fix "$file_path" >/dev/null 2>&1
exit 0