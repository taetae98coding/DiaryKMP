#!/usr/bin/env bash
# 실패한 테스트 하나를 같은 조건으로 여러 번 다시 실행해 항상 실패하는지, 간헐적으로 실패하는지 가른다.
# 각 회차는 --rerun으로 캐시 없이 실행한다. 전체 실행에서만 실패하는 순서 의존 문제는 단독 실행으로 재현되지 않을 수 있으므로,
# 모두 통과하면 그 모듈의 테스트 태스크 전체를 같은 방법으로 다시 돌려 본다.
#
# 사용법: DIARY_AGENTS/scripts/rerun-test.sh <태스크 경로> [테스트 필터] [횟수]   횟수 기본값은 5다.
#   DIARY_AGENTS/scripts/rerun-test.sh :feature:memo:ui:testAndroidHostTest '*MemoListDetailPlaceholderTest*' 5
# 종료 코드: 0 모두 통과, 1 모두 실패, 3 일부만 실패(간헐 실패)
set -uo pipefail

task="${1:?태스크 경로가 필요하다}"
filter="${2:-}"
count="${3:-5}"
repository_root="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$repository_root"

arguments=("$task" --rerun -q)
[[ -n "$filter" ]] && arguments+=(--tests "$filter")

passed=0
failed=0
for ((round = 1; round <= count; round++)); do
  if ./gradlew "${arguments[@]}" >/dev/null 2>&1; then
    passed=$((passed + 1))
    echo "$round/$count 통과"
  else
    failed=$((failed + 1))
    echo "$round/$count 실패"
  fi
done

echo "통과 $passed, 실패 $failed"
if ((failed == 0)); then
  exit 0
elif ((passed == 0)); then
  exit 1
fi
exit 3
