#!/usr/bin/env bash
# 바뀐 .kt 파일이 속한 Gradle 모듈의 태스크 경로를 한 줄에 하나씩 출력한다.
# 바뀐 파일은 unstaged, staged 변경과 untracked 파일을 합치고, base를 주면 base...HEAD의 변경도 더한다. 삭제된 경로는 뺀다.
# 모듈은 파일에서 가장 가까운 상위의 build.gradle.kts 디렉터리이며, 디렉터리 경로가 곧 Gradle 경로다. 루트와 build-logic은 대상이 아니다.
# 테스트 태스크(jvmTest, testAndroidHostTest)는 그 소스셋 디렉터리가 있는 모듈만 출력한다.
#
# 사용법: DIARY_AGENTS/scripts/changed-module-tasks.sh [task] [base]   task 기본값은 detekt다.
#   ./gradlew $(DIARY_AGENTS/scripts/changed-module-tasks.sh) --continue
#   ./gradlew $(DIARY_AGENTS/scripts/changed-module-tasks.sh jvmTest) $(DIARY_AGENTS/scripts/changed-module-tasks.sh testAndroidHostTest) --continue
set -euo pipefail

task="${1:-detekt}"
base="${2:-}"
repository_root="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$repository_root"

case "$task" in
  jvmTest) source_set="jvmTest" ;;
  testAndroidHostTest) source_set="androidHostTest" ;;
  *) source_set="" ;;
esac

{
  git diff --name-only --diff-filter=d
  git diff --cached --name-only --diff-filter=d
  git ls-files --others --exclude-standard
  if [[ -n "$base" ]]; then
    git diff --name-only --diff-filter=d "$base...HEAD"
  fi
} | { grep -E '\.kt$' || true; } | { grep -vE '^build-logic/' || true; } | sed -E 's#/[^/]+$##' | sort -u | while IFS= read -r directory; do
  while [[ "$directory" == */* && ! -f "$directory/build.gradle.kts" ]]; do
    directory="${directory%/*}"
  done
  [[ -f "$directory/build.gradle.kts" ]] || continue
  [[ -z "$source_set" || -d "$directory/src/$source_set" ]] || continue
  echo ":${directory//\//:}:$task"
done | sort -u
