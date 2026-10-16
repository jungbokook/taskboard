#!/bin/bash
# Claude Code 상태 표시줄: stdin JSON에서 모델 이름과 컨텍스트 사용률을 읽고 git 브랜치와 함께 출력
input=$(cat)

model=$(echo "$input" | jq -r '.model.display_name // "unknown"')
used=$(echo "$input" | jq -r '.context_window.used_percentage // 0')
dir=$(echo "$input" | jq -r '.workspace.current_dir // .cwd // empty')

branch=$(git -C "${dir:-.}" branch --show-current 2>/dev/null)

echo "[$model] ctx ${used}% | ${branch:--}"