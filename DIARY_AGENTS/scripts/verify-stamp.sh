#!/usr/bin/env bash
# 커밋 전 검증의 통과 기록을 남기고, 커밋하려는 작업 트리가 그 기록과 맞는지 확인한다.
# 기록은 둘이고 worktree마다 따로 두도록 `git rev-parse --absolute-git-dir` 아래에 남긴다.
#
#   verify  verify-wave(규칙·추적 검토, spotless, detekt, dependencyGuard)의 통과 기록.
#           지문은 기준 브랜치에서 갈라진 뒤의 변경을 -U0 diff로 만든 patch-id다. 줄 번호와 주변 문맥을 보지 않으므로
#           같은 변경을 여러 커밋으로 나누거나 컨플릭 없이 리베이스해도 유지되고, 바뀐 줄이 하나라도 달라지면 무효가 된다.
#   test    test-wave(저장소 전체 테스트, class 파일 이름 검사)의 통과 기록.
#           지문은 테스트 입력이 아닌 경로를 뺀 작업 트리 전체(untracked 포함)의 해시다. 새 base의 파일이 들어오는
#           리베이스 뒤에는 무효가 되고, 같은 작업 트리를 여러 커밋으로 나누거나 테스트 입력이 아닌 문서만 고치면 유지된다.
#
# 기준 브랜치는 origin/HEAD가 가리키는 브랜치(없으면 develop)다. 그 브랜치에서 작업 중이면 upstream을 기준으로 삼는다.
#
# 사용법:
#   DIARY_AGENTS/scripts/verify-stamp.sh record verify|test   각 wave 통과 직후 실행한다.
#   DIARY_AGENTS/scripts/verify-stamp.sh check [verify|test]  기록이 지금 상태와 맞으면 0, 아니면 이유를 출력하고 1.
#                                                             생략하면 커밋 hook용으로, 검증이 필요한 미커밋 변경이 없으면 0, 있으면 둘 다 본다.
#   DIARY_AGENTS/scripts/verify-stamp.sh test-targets         기준 브랜치 이후 바뀐 파일 중 테스트 입력을 출력한다. 비면 test-wave를 건너뛴다.
set -euo pipefail

repository_root="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$repository_root"

git_dir="$(git rev-parse --absolute-git-dir)"

# DIARY_AGENTS.md의 작업 iterator 기준으로 verify-wave가 필요 없는 경로. 하네스 문서, 스펙·테스트 케이스가 아닌 문서, 사이트, CI 설정이다.
filter_verification() {
  { grep -vE '^(DIARY_AGENTS/|\.claude/|\.codex/|\.agents/|\.github/|site/)|^(DIARY_AGENTS|AGENTS|CLAUDE)\.md$' || true; } |
    awk '/^docs\/(spec|testcase)\// || !/\.md$/'
}

# 저장소 전체 테스트의 입력이 아닌 경로. 이 목록에 없는 경로는 테스트 입력으로 본다.
test_exclude='^(docs|DIARY_AGENTS|site|supabase|iosApp|config/detekt|[.]github|[.]claude|[.]codex|[.]agents)/|^app/[^/]+/dependencies/|[.]md$'

default_branch() {
  local branch
  branch="$(git symbolic-ref --quiet --short refs/remotes/origin/HEAD 2>/dev/null || echo origin/develop)"
  echo "${branch#origin/}"
}

base_commit() {
  local branch current upstream
  branch="$(default_branch)"
  current="$(git symbolic-ref --quiet --short HEAD 2>/dev/null || true)"
  if [[ "$current" == "$branch" ]]; then
    upstream="$(git rev-parse --abbrev-ref '@{u}' 2>/dev/null || true)"
    if [[ -n "$upstream" ]]; then
      git merge-base HEAD "$upstream"
    else
      git rev-parse HEAD
    fi
    return
  fi
  git merge-base HEAD "$branch" 2>/dev/null || git merge-base HEAD "origin/$branch" 2>/dev/null || git rev-parse HEAD
}

# untracked 파일까지 담은 임시 index를 만들어 경로를 출력한다. 호출한 쪽이 지운다.
full_index() {
  local index
  index="$(mktemp)"
  cp "$git_dir/index" "$index" 2>/dev/null || true
  GIT_INDEX_FILE="$index" git add -A >/dev/null 2>&1
  echo "$index"
}

# 기준 브랜치 이후 바뀐 파일(삭제 포함, untracked 포함)
changed_since_base() {
  local index base
  index="$(full_index)"
  base="$(base_commit)"
  GIT_INDEX_FILE="$index" git diff --cached --name-only --no-renames "$base"
  rm -f "$index"
}

verify_hash() {
  local index base files
  index="$(full_index)"
  base="$(base_commit)"
  files="$(GIT_INDEX_FILE="$index" git diff --cached --name-only --no-renames "$base" | filter_verification)"
  if [[ -z "$files" ]]; then
    echo "empty"
  else
    local IFS=$'\n'
    set -f
    # shellcheck disable=SC2086
    GIT_INDEX_FILE="$index" git diff --cached -U0 --no-renames --no-color --no-ext-diff "$base" -- $files |
      git patch-id --stable | cut -d' ' -f1
    set +f
  fi
  rm -f "$index"
}

test_hash() {
  local index tree
  index="$(full_index)"
  tree="$(GIT_INDEX_FILE="$index" git write-tree)"
  rm -f "$index"
  git ls-tree -r --full-tree "$tree" | awk -F'\t' -v re="$test_exclude" '$2 !~ re' | git hash-object --stdin
}

test_targets() {
  changed_since_base | awk -v re="$test_exclude" '$0 !~ re'
}

# 검증이 필요한 미커밋 변경. 커밋 hook은 이것이 없으면 막지 않는다.
uncommitted_targets() {
  {
    git diff HEAD --name-only
    git ls-files --others --exclude-standard
  } | sort -u | filter_verification
}

stamp_file() {
  echo "$git_dir/diary-verify-stamp-$1"
}

check_verify() {
  local current
  current="$(verify_hash)"
  [[ "$current" == "empty" ]] && return 0
  if [[ -f "$(stamp_file verify)" && "$(cat "$(stamp_file verify)")" == "$current" ]]; then
    return 0
  fi
  echo "verify-wave 통과 기록이 없거나, 기록한 뒤 바뀐 줄이 달라졌다. 작업 iterator로 돌아가 verify-wave부터 마친다."
  return 1
}

check_test() {
  [[ -z "$(test_targets)" ]] && return 0
  if [[ -f "$(stamp_file test)" && "$(cat "$(stamp_file test)")" == "$(test_hash)" ]]; then
    return 0
  fi
  echo "test-wave 통과 기록이 없거나, 기록한 뒤 테스트 입력이 바뀌었다. 종료 iterator의 test-wave를 마친다."
  return 1
}

case "${1:-}" in
  record)
    case "${2:-}" in
      verify) verify_hash >"$(stamp_file verify)" ;;
      test) test_hash >"$(stamp_file test)" ;;
      *) echo "사용법: $0 record verify|test" >&2; exit 2 ;;
    esac
    echo "$2 통과 상태를 기록했다."
    ;;
  check)
    case "${2:-}" in
      verify) check_verify ;;
      test) check_test ;;
      "")
        targets="$(uncommitted_targets)"
        [[ -z "$targets" ]] && exit 0
        status=0
        check_verify || status=1
        check_test || status=1
        if [[ "$status" -ne 0 ]]; then
          echo "검증이 필요한 변경: $(echo "$targets" | head -5 | tr '\n' ' ')"
          exit 1
        fi
        ;;
      *) echo "사용법: $0 check [verify|test]" >&2; exit 2 ;;
    esac
    ;;
  test-targets)
    test_targets
    ;;
  *)
    echo "사용법: $0 record verify|test | check [verify|test] | test-targets" >&2
    exit 2
    ;;
esac
