#!/usr/bin/env bash
# verify-wave를 통과한 작업 트리를 기록하고, 커밋하려는 작업 트리가 그 상태 그대로인지 확인한다.
# 지문은 untracked 파일까지 포함한 작업 트리 전체의 tree 해시라, 같은 작업 트리를 여러 커밋으로 나눠도 바뀌지 않는다.
# 기록은 worktree마다 따로 두도록 `git rev-parse --git-dir` 아래에 남긴다.
#
# 사용법:
#   DIARY_AGENTS/scripts/verify-stamp.sh record   verify-wave 통과 직후 실행한다.
#   DIARY_AGENTS/scripts/verify-stamp.sh check    검증이 필요한 변경이 없거나 기록과 같으면 0, 아니면 이유를 출력하고 1.
set -euo pipefail

repository_root="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$repository_root"

stamp_file="$(git rev-parse --absolute-git-dir)/diary-verify-stamp"

working_tree_hash() {
  local index
  index="$(mktemp)"
  cp "$(git rev-parse --absolute-git-dir)/index" "$index" 2>/dev/null || true
  GIT_INDEX_FILE="$index" git add -A >/dev/null 2>&1
  GIT_INDEX_FILE="$index" git write-tree
  rm -f "$index"
}

# DIARY_AGENTS.md의 작업 iterator 기준으로 verify-wave가 필요한 변경만 남긴다.
# 하네스 문서, 스펙·테스트 케이스가 아닌 문서, 사이트, CI 설정만 바뀌었으면 검증 없이 커밋할 수 있다.
verification_targets() {
  {
    git diff HEAD --name-only
    git ls-files --others --exclude-standard
  } | sort -u | grep -vE '^(DIARY_AGENTS/|\.claude/|\.codex/|\.github/|site/)|^(DIARY_AGENTS|AGENTS|CLAUDE)\.md$' |
    awk '/^docs\/(spec|testcase)\// || !/\.md$/' || true
}

case "${1:-}" in
  record)
    working_tree_hash >"$stamp_file"
    echo "verify-wave 통과 상태를 기록했다."
    ;;
  check)
    targets="$(verification_targets)"
    [[ -z "$targets" ]] && exit 0
    if [[ -f "$stamp_file" && "$(cat "$stamp_file")" == "$(working_tree_hash)" ]]; then
      exit 0
    fi
    echo "verify-wave를 통과한 뒤 작업 트리가 바뀌었거나 통과 기록이 없다. 작업 iterator로 돌아가 verify-wave부터 마친다."
    echo "검증이 필요한 변경: $(echo "$targets" | head -5 | tr '\n' ' ')"
    exit 1
    ;;
  *)
    echo "사용법: $0 record|check" >&2
    exit 2
    ;;
esac
