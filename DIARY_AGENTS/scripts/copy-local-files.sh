#!/usr/bin/env bash
# 현재 worktree에 없는 로컬 전용 파일을 기본 worktree에서 복사한다.
# 대상 경로는 DIARY_AGENTS/local-files.md의 목록 표 첫 열에서 읽으므로, 파일을 더할 때는 그 표만 고친다.
# 에이전트 세션이 시작될 때 hook으로 실행되며, 이미 있는 파일은 덮어쓰지 않고 파일 내용은 출력하지 않는다.
#
# 사용법: DIARY_AGENTS/scripts/copy-local-files.sh
set -euo pipefail

repository_root="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$repository_root"

main_worktree="$(git worktree list --porcelain | awk 'NR == 1 && $1 == "worktree" { sub(/^worktree /, ""); print }')"
if [[ -z "$main_worktree" || "$(cd "$main_worktree" && pwd -P)" == "$(pwd -P)" ]]; then
  exit 0
fi

copied=()
missing=()
while IFS= read -r path; do
  [[ -e "$path" ]] && continue
  source_path="$main_worktree/$path"
  if [[ ! -f "$source_path" ]]; then
    missing+=("$path")
    continue
  fi
  mkdir -p "$(dirname "$path")"
  cp -p "$source_path" "$path"
  if cmp -s "$source_path" "$path"; then
    copied+=("$path")
  else
    rm -f "$path"
    missing+=("$path")
  fi
done < <(sed -nE 's/^\| `([^`]+)` \|.*/\1/p' DIARY_AGENTS/local-files.md)

if ((${#copied[@]} > 0)); then
  echo "기본 worktree에서 로컬 전용 파일을 복사했다: ${copied[*]}"
fi
if ((${#missing[@]} > 0)); then
  echo "기본 worktree에도 없어 복사하지 못한 로컬 전용 파일: ${missing[*]}"
fi
