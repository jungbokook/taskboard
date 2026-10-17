#!/bin/bash
# PreToolUse(Edit|Write) 훅: 보호 파일을 직접 수정하지 못하게 막는다.
# exit 2로 끝나면 도구 호출이 차단되고, stderr 내용이 Claude에게 전달된다.

file_path=$(jq -r '.tool_input.file_path // empty')

case "$file_path" in
  *.git/*)
    echo ".git/ 내부 파일은 직접 수정 금지. git 명령(git commit, git config 등)으로 바꾸세요." >&2
    exit 2
    ;;
  *package-lock.json*)
    echo "package-lock.json은 직접 수정 금지. npm install/npm uninstall로 바꾸세요." >&2
    exit 2
    ;;
  *gradle/wrapper/*)
    echo "gradle/wrapper/ 파일은 직접 수정 금지. ./gradlew wrapper --gradle-version <버전>으로 바꾸세요." >&2
    exit 2
    ;;
esac

exit 0