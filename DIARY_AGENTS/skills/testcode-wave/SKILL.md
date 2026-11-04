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

테스트 이름의 설명은 짧게 쓴다. 이름이 class 파일 이름에 들어가는데, CI의 Linux는 파일 이름을 UTF-8 255바이트까지만 허용하고 한글은 한 글자가 3바이트다. TC ID를 둘 이상 붙이는 테스트는 특히 설명을 줄인다. 한도를 넘었는지는 `verify-wave`의 `class 파일 이름 검사`가 확인한다.

자동화할 수 없는 케이스를 조용히 생략하지 않는다. 해당 케이스 아래에 `작성하지 않는 이유`를 기록하고, 작업 결과에도 미작성 ID를 요약한다. 테스트 실패를 피하려고 기대 결과를 약화하거나 문서와 다른 동작에 맞추지 않는다.

## 작성 중 확인

테스트를 작성하는 동안에는 바뀐 모듈의 테스트만 돌려 확인한다. 저장소 전체 실행은 `verify-wave`가 한 번만 맡는다.

```bash
./gradlew $(DIARY_AGENTS/scripts/changed-module-tasks.sh jvmTest) $(DIARY_AGENTS/scripts/changed-module-tasks.sh testAndroidHostTest) --continue
```

작성을 마치면 `DIARY_AGENTS/scripts/check-testcase-trace.py <slug>`로 ID 누락과 문서에 없는 ID가 없는지 확인한다.

## 추가 테스트

문서 케이스를 모두 구현한 뒤 회귀 위험과 구현 계약을 검토해 필요한 테스트를 추가한다.

- 경계값, 빈 값, 중복 입력, 반복 호출처럼 결함이 발생하기 쉬운 입력
- 직렬화·역직렬화, 저장·복원, 상태 전이처럼 왕복 보존이 필요한 계약
- 코루틴 취소, 동시 실행, 재시도, 중복 이벤트처럼 실행 순서에 민감한 동작
- 오류 전파와 fallback처럼 실패 시에도 보장해야 하는 동작
- 수정한 분기와 과거 결함의 재발을 막는 회귀 시나리오
- 테스트 케이스가 `실제 화면의 픽셀, 색상` 기준으로 판정하지 않는 시각 표현을 화면 캡처로 결정적으로 확인하는 회귀 테스트. 예: 카드와 칩의 컬러 표시. 이 테스트는 케이스의 `작성하지 않는 이유`를 바꾸지 않으며 TC ID를 붙이지 않는다

추가 테스트는 새로운 제품 정책을 임의로 확정할 수 없다. 외부에서 관찰되는 동작을 새로 정해야 한다면 스펙이나 테스트 케이스의 공백으로 알린다. 문서 케이스를 보강하는 테스트에는 기존 TC ID를 쓰고, 구현 계약만 검증하는 테스트에는 TC ID를 붙이지 않아 문서 커버리지와 구분한다.

## 소스셋과 도구

테스트는 다음 두 소스셋에만 작성한다. `commonTest`에는 테스트를 두지 않고, `테스트 데이터`의 생성 규칙만 둔다.

| 소스셋 | 대상 | 프레임워크 |
| --- | --- | --- |
| `jvmTest` | Compose UI를 다루지 않는 유닛 테스트(feature의 ViewModel, domain, data) | Kotest |
| `androidHostTest` | Compose UI 테스트. 실행 태스크는 `testAndroidHostTest` | JUnit4 |

- `androidHostTest`의 assertion도 JUnit4 대신 Kotest assertions(`shouldBe` 등)를 쓴다.
- Flow 검증은 Turbine(`flow.test { ... }`)을 쓴다. one-shot Effect는 `awaitItem()`으로 수신을, `expectNoEvents()`로 추가 발행이 없음을 검증한다.
- UseCase 테스트는 Kotest `BehaviorSpec`의 Given/When/Then으로 작성한다.
- `Pager`가 만든 흐름을 `asSnapshot`으로 검증하는 테스트는 `runTest` 안에서 실행한다. paging-testing의 `asSnapshot`과(3.5.3까지 확인) 그 안의 `refresh`·`scrollTo`·`appendScrollWhile`은 로딩이 끝났는지를 상태가 1ms 동안 바뀌지 않는지(`debounce`)로 판단한다. `runTest` 밖에서는 이 1ms가 실제 시간이라, 부하가 걸리면 새 세대나 다음 페이지가 도착하기 전에 대기가 끝나 이전 목록을 돌려주고 전체 실행에서만 간헐적으로 실패한다. `FileRepositoryImplTest`의 다시 불러오기 테스트가 이렇게 실패했다. `runTest` 안에서는 1ms가 가상 시간이라 밀린 작업을 모두 처리한 뒤에야 대기가 끝난다. 라이브러리가 시간 대신 세대 완료를 기다리게 바뀌면 이 규칙과 `runTest` 감싸기를 지워도 된다. `flowOf(PagingData.from(list))`처럼 한 세대뿐인 정적 데이터는 대상이 아니다.

## Mock

mock 라이브러리는 MockK를 쓴다. 외부 경계와 협력 객체는 MockK mock으로 제어하고, 자동화를 위한 별도 Fake 구현은 작성하거나 유지하지 않는다.

예외로, [compose.md](../../rules/compose.md)의 `플랫폼별 표현은 Override로 바꿔 끼운다`가 정한 `XxxOverride`는 `@Composable` 멤버를 MockK로 흉내 낼 수 없으므로 테스트가 직접 구현해 `LocalXxxOverride`에 넣는다. 구현은 Scope를 기록하고 테스트가 관찰할 노드를 그리는 데서 그치며 판정 규칙을 담지 않는다. 한 파일에서만 쓰면 그 파일에 `private`으로 두고, 같은 모듈의 여러 파일이 쓰면 그 모듈 테스트 소스셋에 하나만 둔다.

반환값, 연속 응답, 예외, 호출 기록, 변경 가능한 상태가 필요하면 MockK의 stub, answer, verify와 mock이 반환하는 테스트 데이터 또는 상태 스트림을 쓴다.

플랫폼이나 라이브러리가 `TestLifecycleOwner`, `TestDispatcher`처럼 상태를 제어하는 공식 테스트 유틸리티를 제공하면 직접 구현하거나 MockK로 흉내 내지 않고 그 유틸리티를 먼저 쓴다.

## 테스트 데이터

테스트 데이터는 Kotest property의 `Arb`로 항상 임의 생성하고, 검증은 생성된 인스턴스의 필드 값을 참조한다. 값 하나는 `Arb.memo().next()`처럼 꺼내고, 특정 필드만 정해야 하면 `Arb.memo().next().copy(isFinished = false)`처럼 만들어진 인스턴스를 `copy`로 바꾼다.

생성 규칙은 리플렉션 없이 생성자를 직접 호출하는 `Arb.Companion` 확장 함수로 둔다. 이후 `commonTest`로 옮길 테스트가 iOS와 wasmJs에서도 같은 규칙을 쓰게 하기 위해서다. Kotest의 `Arb.bind<T>()`처럼 리플렉션으로 생성자를 채우는 API는 JVM에서만 동작하므로 쓰지 않는다. 예외로 화면 키 클래스처럼 대상 클래스를 런타임에야 알 수 있는 JVM 테스트만 `Arb.bind(arbs, kClass)`를 쓴다.

생성 규칙은 다음 두 모듈이 소유하고 테스트 파일에서 같은 타입의 규칙을 다시 만들지 않는다. 파일마다 규칙을 두면 같은 타입을 새 테스트가 다시 만들지 못하는 함정이 반복된다.

| 모듈 | 두는 것 | 주입 |
| --- | --- | --- |
| `:library:fixture` | 라이브러리 타입의 규칙과 Kotest 기본 규칙을 대신하는 규칙. `kotest-property`를 함께 노출한다 | `KotestPrimitivePlugin`이 모든 모듈의 `commonTest`에 주입한다 |
| `:core:testing` | `:core:model`과 `:core:*:api`의 모델·엔티티 규칙(`Arb.memo()`, `Arb.tagLocalEntity()` 등)과 고정 값 헬퍼 | 쓰는 모듈이 테스트 소스셋에 의존성을 직접 선언한다 |

`:library:fixture`가 Kotest 기본 규칙 대신 쓰게 하는 타입은 다음과 같다.

| 타입 | 규칙 | 이유 |
| --- | --- | --- |
| `Instant` | `Arb.secondPrecisionInstant()` | 저장소는 에포크 밀리초로, 일부 원격 계약은 에포크 초로 다루므로 그보다 정밀한 값은 왕복하면 달라진다. 1970~2100년의 초 정밀도 값을 만든다. 시각을 직접 만들어 넣을 때도 이 정밀도를 지킨다 |
| `Uuid` | `Arb.kotlinUuid()` | Kotest의 `Arb.uuid()`는 `java.util.UUID`를 만든다 |
| `LocalDate`, `LocalTime`, `LocalDateTime` | `Arb.kotlinxLocalDate()` 등 | Kotest의 `Arb.localDate()` 등은 `java.time` 타입을 만든다 |
| `LocalDateRange` | `Arb.localDateRange()` | 시작일이 종료일보다 뒤가 아닌 30일 이내의 기간을 만든다 |
| `ByteArray` | `Arb.randomByteArray()` | Kotest의 `Arb.byteArray()`는 길이와 내용 규칙을 따로 받는다 |
| `Double`, `Float` | Kotest의 `Arb.numericDouble()`, `Arb.numericFloat()` | `Arb.double()`과 `Arb.float()`는 `next()`로 꺼내도 NaN과 무한대를 내므로 직렬화나 비교가 깨진다 |

모델이 담는 목록·집합·맵은 `MAX_COLLECTION_SIZE`(3)개까지 만든다. 크기 자체를 검증하는 테스트는 목록을 직접 만든다.

`Arb.string()`은 약 1%가 빈 문자열이다. 비어 있지 않아야 판정이 성립하면 `Arb.nonBlankString()`을 쓰고, 빈 문자열 자체가 목적이면 직접 지정한다.

`:core:testing`의 규칙은 `<타입 이름의 lowerCamel>()`로 이름 짓고, 도메인 패키지(`core.testing.memo` 등)에 계층별 파일(`MemoModelArb.kt`, `MemoLocalEntityArb.kt`, `MemoRemoteEntityArb.kt`)로 둔다. enum은 `Arb.enum<T>()`, object는 `Arb.constant(T)`를 그대로 쓰고 별도 규칙을 두지 않는다. sealed 타입은 하위 타입 규칙을 `Arb.choice`로 묶는다. 생성자가 private인 타입은 공개된 생성 경로로 만든다(`Arb.length()`).

엔티티와 모델의 일부 필드를 고정한 규칙(`Arb.localTag(isFinished, isDeleted)` 등)은 `Arb`를 돌려주는 `Arb.Companion` 확장 함수로, 여러 조건을 나열하는 목록(`contactDetailCaseList()` 등)은 일반 함수로 `:core:testing`에 둔다. 무작위 기본값이 필요한 파라미터는 `title: String? = null`로 받아 `arbitrary` 안에서 `title ?: ...`로 채워, 같은 `Arb`에서 꺼낸 값마다 새로 만들어지게 한다. 두 모듈 이상의 테스트가 같은 타입을 만들면 그 규칙은 `:core:testing`으로 옮긴다. `:core:testing`이 의존할 수 없는 타입(feature·compose·domain·impl 모듈의 타입)은 그 타입을 쓰는 모듈의 `commonTest`에 `internal`로 둔다. 다만 그 모듈에 `jvmTest` 테스트가 없으면 `commonTest`의 소스만으로도 `jvmTest`가 테스트를 찾지 못해 실패하므로, 쓰는 소스셋(`androidHostTest`)에 둔다. `androidMain`에만 있는 타입도 `androidHostTest`에 둔다.

fixture는 최상위 프로퍼티가 아니라 각 테스트 본문 안에서 생성한다. 테스트마다 다른 값이 생성되어 테스트 간 상관관계가 없고, 실패한 테스트에 쓰인 값을 그 테스트 안에서 바로 확인할 수 있다.

빈 값 확인처럼 특정 값 자체가 검증 대상인 경우에만 고정 값을 쓴다. 예를 들어 언어별 UI 문구 검증은 실제 리소스 문자열을 고정 값으로 쓴다.

Compose UI 테스트에서 화면에 입력하거나 표시해 글자로 노드를 찾는 사용자 데이터(제목, 설명, 항목 내용, URL 등)는 예외로, 읽기 쉬운 고정 문자열 상수를 써도 된다. 임의 문자열은 길이와 공백, 줄바꿈에 따라 표시와 노드 탐색이 흔들리고, 실패한 화면에서 어떤 값을 찾던 중인지 알아보기 어렵다. 이 예외는 화면에 보이는 글에만 적용하며, 식별자·시각처럼 화면에서 글자로 찾지 않는 값과 jvmTest의 데이터는 그대로 `Arb`로 만든다.

## Compose UI 테스트

Compose UI 테스트는 `androidHostTest`에서 Robolectric을 사용해 작성한다.

- 상태 홀더의 순수한 상태 전이와 제품 규칙은 생성자로 만든 인스턴스를 사용해 `jvmTest`에서 검증한다.
- 애니메이션 suspend 함수처럼 `MonotonicFrameClock` 등 Compose 런타임 서비스가 필요한 동작은 `jvmTest`에서 클록을 직접 구현하지 않고, `androidHostTest`에서 Compose 테스트 규칙이 제공하는 클록으로 검증한다. 상태 홀더는 생성자로 만들고 `setContent`의 `LaunchedEffect`에서 동작을 실행한 뒤 `waitForIdle`로 완료를 기다린다.
- `rememberXxxState`처럼 Composable 팩토리가 제공하는 초기화, 같은 Composition 안의 인스턴스 유지, 저장·복원 동작은 그 팩토리로 상태를 생성해 `androidHostTest`에서 별도로 검증한다.
- 스펙이 화면 재생성 후 상태 유지를 요구하고 팩토리가 `rememberSaveable` 또는 저장 가능한 내비게이션 상태를 쓰면 `StateRestorationTester`로 실제 복원을 검증한다. 이 테스트를 생성자 기반 상태 전이 테스트로 대체하지 않는다.
- `StateRestorationTester.setContent`에서 최신 상태 홀더를 캡처하고, 상태 변경과 검증은 `runOnIdle`에서 수행한다. 복원 후에는 현재 화면뿐 아니라 스펙이 요구하는 뒤로가기 결과처럼 관찰되는 전환 이력도 검증한다.
- Pager나 Flow의 결과가 이어서 전달되기를 기다리는 Robolectric 화면 테스트는 테스트마다 시작 전에 `AndroidUiDispatcher.Main`의 남은 예약 상태를 비운다. 조회 결과는 앱 전체가 함께 쓰는 이 디스패처로 화면에 전달되는데, Robolectric은 테스트가 끝날 때 메인 스레드 대기열만 비우고 디스패처는 "이미 예약했다"는 표시와 앞선 테스트가 남긴 작업을 그대로 들고 있어 뒤이은 테스트의 작업을 다시 예약하지 않는다. 비우지 않으면 앞선 테스트에 따라 결과가 전달되지 않아 전체 실행에서만 실패한다. 비우는 함수는 모듈의 `androidHostTest` 루트 패키지에 있는 `AndroidUiDispatcherTestReset.kt`의 `resetAndroidUiDispatcher()`를 `@Before`에서 부르고, 그 파일이 없는 모듈에는 같은 파일을 둔다.

