---
name: testcode-wave
description: Diary KMP 프로젝트에서 사용자가 유닛 테스트 코드 작성, 테스트 케이스 문서의 자동화, 테스트 소스셋 선택, 테스트 프레임워크나 mock 라이브러리 선택, Compose UI 테스트 작성을 요청할 때 사용한다. 테스트 케이스 문서의 100% 구현을 목표로 하고 개발자 관점에서 필요한 추가 테스트도 설계하며, 테스트는 jvmTest와 androidHostTest 두 소스셋에만 작성한다.
---

# Testcode Wave

`docs/testcase`의 테스트 케이스를 자동화한 테스트 코드를 작성한다.

대상 문서를 찾는 절차는 `DIARY_AGENTS/wave.md`의 `대상 문서 탐색`을 따른다. 스펙이나 테스트 케이스의 공백을 알리는 형식은 `DIARY_AGENTS/confirm.md`를 따른다. 테스트 케이스 문서의 형식과 고유 ID 규칙은 `docs/testcase/README.md`를 따른다.

## 범위와 추적성

관련 스펙과 테스트 케이스 문서를 먼저 읽고, 작업 범위의 모든 케이스를 목록화한다. `jvmTest`와 `androidHostTest`에서 결정적으로 자동화할 수 있는 케이스는 100% 작성하는 것을 목표로 한다.

각 케이스에는 그 고유 ID를 이름에 포함한 테스트를 하나 이상 연결한다. 테스트 데이터만 다른 동일 행동은 한 테스트에서 순회할 수 있지만, 기대 결과가 다른 케이스를 하나로 합치지 않는다.

자동화할 수 없는 케이스를 조용히 생략하지 않는다. 해당 케이스 아래에 `작성하지 않는 이유`를 기록하고, 작업 결과에도 미작성 ID를 요약한다. 테스트 실패를 피하려고 기대 결과를 약화하거나 문서와 다른 동작에 맞추지 않는다.

## 추가 테스트

문서 케이스를 모두 구현한 뒤 회귀 위험과 구현 계약을 검토해 필요한 테스트를 추가한다.

- 경계값, 빈 값, 중복 입력, 반복 호출처럼 결함이 발생하기 쉬운 입력
- 직렬화·역직렬화, 저장·복원, 상태 전이처럼 왕복 보존이 필요한 계약
- 코루틴 취소, 동시 실행, 재시도, 중복 이벤트처럼 실행 순서에 민감한 동작
- 오류 전파와 fallback처럼 실패 시에도 보장해야 하는 동작
- 수정한 분기와 과거 결함의 재발을 막는 회귀 시나리오

추가 테스트는 새로운 제품 정책을 임의로 확정할 수 없다. 외부에서 관찰되는 동작을 새로 정해야 한다면 스펙이나 테스트 케이스의 공백으로 알린다. 문서 케이스를 보강하는 테스트에는 기존 TC ID를 쓰고, 구현 계약만 검증하는 테스트에는 TC ID를 붙이지 않아 문서 커버리지와 구분한다.

## 소스셋과 도구

테스트는 다음 두 소스셋에만 작성한다. `commonTest`는 쓰지 않는다.

| 소스셋 | 대상 | 프레임워크 |
| --- | --- | --- |
| `jvmTest` | Compose UI를 다루지 않는 유닛 테스트(feature의 ViewModel, domain, data) | Kotest |
| `androidHostTest` | Compose UI 테스트. 실행 태스크는 `testAndroidHostTest` | JUnit4 |

- `androidHostTest`의 assertion도 JUnit4 대신 Kotest assertions(`shouldBe` 등)를 쓴다.
- Flow 검증은 Turbine(`flow.test { ... }`)을 쓴다. one-shot Effect는 `awaitItem()`으로 수신을, `expectNoEvents()`로 추가 발행이 없음을 검증한다.
- UseCase 테스트는 Kotest `BehaviorSpec`의 Given/When/Then으로 작성한다.

## Mock

mock 라이브러리는 MockK를 쓴다. 외부 경계와 협력 객체는 MockK mock으로 제어하고, 자동화를 위한 별도 Fake 구현은 작성하거나 유지하지 않는다.

반환값, 연속 응답, 예외, 호출 기록, 변경 가능한 상태가 필요하면 MockK의 stub, answer, verify와 mock이 반환하는 테스트 데이터 또는 상태 스트림을 쓴다.

플랫폼이나 라이브러리가 `TestLifecycleOwner`, `TestDispatcher`처럼 상태를 제어하는 공식 테스트 유틸리티를 제공하면 직접 구현하거나 MockK로 흉내 내지 않고 그 유틸리티를 먼저 쓴다.

## 테스트 데이터

테스트 데이터는 FixtureMonkey로 항상 임의 생성하고, 검증은 생성된 인스턴스의 필드 값을 참조한다.

`FixtureMonkey` 인스턴스는 `:library:fixture-monkey`의 `diaryFixtureMonkey()`로 만든다. 이 모듈은 모든 테스트 소스셋에 자동으로 주입되므로 의존성을 따로 선언하지 않는다. 빌더에 무언가를 더 이어 붙여야 하면 `diaryFixtureMonkeyBuilder()`를 쓴다.

`FixtureMonkey.builder()`를 테스트 파일에서 직접 호출하지 않는다. 특정 타입의 생성 규칙이 필요하면 그 파일이 아니라 `:library:fixture-monkey`의 구성에 더해 저장소 전체가 같은 규칙을 쓴다. 파일마다 구성을 두면 같은 타입을 새 테스트가 다시 만들지 못하는 함정이 반복된다. 그 구성이 이미 다루는 타입은 다음과 같다.

| 타입 | 왜 구성이 필요한가 | 고정할 때 |
| --- | --- | --- |
| `Instant` | FixtureMonkey가 만들면 표현 범위를 넘겨 약 7%가 실패하고, 성공해도 20만 년 전 같은 값이 나온다. 구성이 1970~2100년의 초 정밀도 값을 만든다 | `setExp`로 고정한 값이 그대로 유지된다 |
| `LocalDateRange` | `Iterable`을 구현해 FixtureMonkey가 컨테이너로 오인하고 생성자 인자를 채우지 못한다 | `setExp`가 덮어써지므로 만들어진 인스턴스를 `copy`로 바꾼다 |

`Instant`는 초 정밀도로 만든다. 저장소는 에포크 밀리초로, 일부 원격 계약은 에포크 초로 다루므로 그보다 정밀한 값은 왕복하면 달라진다. 시각을 직접 만들어 넣을 때도 이 정밀도를 지킨다.

생성한 문자열은 약 3%가 빈 문자열이다. 빈 값이면 결과가 달라지는 테스트는 생성 값을 그대로 쓰지 말고 값을 직접 정하거나 앞에 고정 문자열을 붙인다.

엔티티와 모델의 고정 값을 만드는 헬퍼(`localTag(isFinished, isDeleted)`, `contactDetailCaseList()` 등)는 테스트 파일마다 다시 쓰지 않고 `:core:testing`에 `FixtureMonkey` 확장 함수로 둔다. 두 모듈 이상의 테스트가 같은 엔티티를 만들면 그 헬퍼는 이 모듈로 옮긴다. `:core:testing`은 `core:database:api`, `core:network:api`, `core:model`에 의존하므로 `:library:*`가 아니라 `:core:*`에 있고, 쓰는 모듈의 `jvmTest`가 의존성을 직접 선언한다.

`FixtureMonkey` 인스턴스는 파일 최상위에 한 번 만들어 공유하고, fixture는 최상위 프로퍼티가 아니라 각 테스트 본문 안에서 생성한다. 테스트마다 다른 값이 생성되어 테스트 간 상관관계가 없고, 실패한 테스트에 쓰인 값을 그 테스트 안에서 바로 확인할 수 있다. `FixtureMonkey` 프로퍼티는 명시적 타입을 선언해야 한다(타입 추론에 checkerframework 어노테이션이 섞여 컴파일 에러가 발생한다).

빈 값 확인처럼 특정 값 자체가 검증 대상인 경우에만 고정 값을 쓴다. 예를 들어 언어별 UI 문구 검증은 실제 리소스 문자열을 고정 값으로 쓴다.

## Compose UI 테스트

Compose UI 테스트는 `androidHostTest`에서 Robolectric을 사용해 작성한다.

- 상태 홀더의 순수한 상태 전이와 제품 규칙은 생성자로 만든 인스턴스를 사용해 `jvmTest`에서 검증한다.
- 애니메이션 suspend 함수처럼 `MonotonicFrameClock` 등 Compose 런타임 서비스가 필요한 동작은 `jvmTest`에서 클록을 직접 구현하지 않고, `androidHostTest`에서 Compose 테스트 규칙이 제공하는 클록으로 검증한다. 상태 홀더는 생성자로 만들고 `setContent`의 `LaunchedEffect`에서 동작을 실행한 뒤 `waitForIdle`로 완료를 기다린다.
- `rememberXxxState`처럼 Composable 팩토리가 제공하는 초기화, 같은 Composition 안의 인스턴스 유지, 저장·복원 동작은 그 팩토리로 상태를 생성해 `androidHostTest`에서 별도로 검증한다.
- 스펙이 화면 재생성 후 상태 유지를 요구하고 팩토리가 `rememberSaveable` 또는 저장 가능한 내비게이션 상태를 쓰면 `StateRestorationTester`로 실제 복원을 검증한다. 이 테스트를 생성자 기반 상태 전이 테스트로 대체하지 않는다.
- `StateRestorationTester.setContent`에서 최신 상태 홀더를 캡처하고, 상태 변경과 검증은 `runOnIdle`에서 수행한다. 복원 후에는 현재 화면뿐 아니라 스펙이 요구하는 뒤로가기 결과처럼 관찰되는 전환 이력도 검증한다.
- Pager나 Flow의 결과가 이어서 전달되기를 기다리는 Robolectric 화면 테스트는 테스트마다 시작 전에 `AndroidUiDispatcher.Main`의 남은 예약 상태를 비운다. 조회 결과는 앱 전체가 함께 쓰는 이 디스패처로 화면에 전달되는데, Robolectric은 테스트가 끝날 때 메인 스레드 대기열만 비우고 디스패처는 "이미 예약했다"는 표시와 앞선 테스트가 남긴 작업을 그대로 들고 있어 뒤이은 테스트의 작업을 다시 예약하지 않는다. 비우지 않으면 앞선 테스트에 따라 결과가 전달되지 않아 전체 실행에서만 실패한다.

