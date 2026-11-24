---
name: test-wave
description: Diary KMP 프로젝트의 종료 iterator에서 rebase-wave 다음, commit-wave 전에 사용한다. 저장소 전체 유닛 테스트(jvmTest, testAndroidHostTest)와 class 파일 이름 검사를 한 번 실행하고, 하나라도 실패하면 고치지 않고 보고한 뒤 다음 단계로 넘어가지 않는다. 통과하면 커밋 hook이 확인할 test 통과 기록을 남긴다.
---

# Test Wave

현재 작업 트리가 저장소 전체 테스트를 통과하는지 커밋 직전에 한 번 확인하는 게이트다. 리베이스 뒤에 실행하므로 새 base 위에서의 컴파일과 테스트를 함께 확인한다.

작업 중에는 `testcode-wave`가 바뀐 모듈의 테스트만 돌린다. 이 wave는 한 모듈의 변경이 그 모듈을 쓰는 다른 모듈을 깨뜨렸는지를 확인하므로, 실행 범위를 일부 모듈로 좁히지 않는다. 바뀌지 않은 모듈은 빌드 캐시가 다시 실행하지 않는다.

## 선행 조건

1. `DIARY_AGENTS/scripts/verify-stamp.sh check verify`가 통과해야 한다. 실패하면 `verify-wave`를 통과하지 않았거나 그 뒤 바뀐 줄이 있는 것이므로, 테스트를 돌리지 않고 보고한 뒤 멈춘다.
2. `DIARY_AGENTS/scripts/verify-stamp.sh test-targets`가 아무것도 출력하지 않으면 테스트 입력이 바뀌지 않은 것이므로 건너뛰고 결과에 남긴다. 테스트 입력이 아닌 경로는 이 스크립트가 소유한다.
3. `DIARY_AGENTS/scripts/verify-stamp.sh check test`가 통과하면 마지막 통과 뒤 테스트 입력이 바뀌지 않은 것이므로 다시 실행하지 않는다.

## 실행

Gradle 명령끼리는 빌드 락을 두고 경합하므로 병렬로 실행하지 않는다.

1. `./gradlew jvmTest testAndroidHostTest --continue`
2. `DIARY_AGENTS/scripts/check-class-file-name.py`

`--continue`로 한 모듈이 실패해도 나머지 결과까지 모은다.

### class 파일 이름 검사

백틱 테스트 이름은 class 파일 이름에 그대로 들어가는데, CI의 Linux는 파일 이름을 UTF-8 255바이트까지만 허용해 한글 이름이 길면 로컬(macOS)에서는 통과하고 CI에서만 컴파일이 실패한다. 어떤 람다가 별도 class를 만드는지는 소스로 알 수 없으므로, 테스트를 실행해 컴파일 결과가 남은 뒤 `check-class-file-name.py`로 각 모듈의 `build/classes`를 검사한다. `문제 없음`을 출력하고 0으로 끝나야 통과다.

### 코드를 고치지 않는 재실행

`jvmTest`가 `initializationError`로 실패하고 원인에 `metro.hints` 패키지 클래스와 `NoClassDefFoundError`가 함께 있으면, 빌드 캐시가 지우거나 이름을 바꾼 클래스를 참조하는 옛 Metro 힌트 클래스를 복원한 것이다. 실패한 모듈마다 `./gradlew :<모듈>:compileTestKotlinJvm --rerun-tasks --no-build-cache`로 다시 컴파일한 뒤 `jvmTest testAndroidHostTest --continue`를 한 번 더 실행하고, 그 결과로 통과를 판정한다. 재실행 사실은 결과에 남긴다.

이번 변경과 관계없어 보이는 테스트가 실패하면 `bash DIARY_AGENTS/scripts/rerun-test.sh <태스크 경로> <테스트 필터>`로 다시 돌려 항상 실패하는지(종료 코드 1), 간헐적으로 실패하는지(3) 가린 뒤 함께 보고한다. 간헐 실패라도 통과로 처리하지 않는다.

## 실패 처리

명령이 하나라도 실패하거나 실행할 수 없으면 이 wave를 통과하지 않은 것으로 보고, 다음 단계로 넘어가지 않는다. 이 wave에서는 코드도 테스트도 고치지 않는다.

다음을 보고하고 지시를 받는다.

- 실패한 명령과 태스크 경로
- 실패한 테스트와 핵심 오류, 간헐 실패 판정 결과
- class 파일 이름 검사가 보고한 파일

사용자가 고치라고 하면 작업 iterator로 돌아가 고친다. 테스트 이름이 길어 class 파일 이름 검사에 걸렸으면 `$` 앞의 클래스와 그 뒤의 테스트 이름으로 테스트를 찾아, TC ID는 그대로 두고 설명을 줄인다. 고친 뒤 `verify-wave`까지 마치고 종료 iterator를 처음부터 다시 시작한다.

## 통과 조건

테스트와 class 파일 이름 검사가 모두 성공하면 `DIARY_AGENTS/scripts/verify-stamp.sh record test`를 실행하고 다음 단계로 넘어간다.

통과 기록은 테스트 입력의 지문이라, 기록한 뒤 테스트 입력이 하나라도 바뀌면 `git commit`을 막는 hook이 이 wave를 다시 요구한다. 테스트 입력이 아닌 문서만 바뀌었으면 기록이 유지된다.
