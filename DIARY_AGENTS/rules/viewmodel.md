# ViewModel 규칙

## 화면당 ViewModel 분리

**한 화면에 ViewModel을 하나만 두지 않고, 관심사마다 나눈다.** 하나로 두면 관계없는 상태가 한 `combine`에 묶여 함께 다시 계산되고, 테스트가 나머지 의존성까지 mock으로 채워야 한다.

**나누는 단위는 UiState다.** 하나의 UiState를 만드는 데 필요한 값은 한 ViewModel이 모아서 조합하고, UiState가 다르면 ViewModel을 나눈다. 아래 기준은 UiState를 나눌지 판단할 때 함께 본다.

- **시작 트리거가 다르면 나눈다.** 화면 진입에 시작하는 조회와 사용자가 눌러야 시작하는 동작은 같은 ViewModel에 두지 않는다.
- **화면에서 사라져도 나머지가 그대로면 나눈다.** 동기화 진행 표시, 필터 적용 표시처럼 곁들여진 상태가 여기 해당한다.
- **같은 대상의 CRUD는 나누지 않는다.** 한 메모의 조회·수정·완료·복사·삭제처럼 대상이 하나이고 동작이 그 대상의 수명주기를 이루면 한 ViewModel에 둔다.

**서로 다른 도메인에서 온다는 이유만으로는 나누지 않는다.** 같은 UiState의 필드로 함께 표시된다면 그 조합은 ViewModel의 일이다. 예: `PlaceAddUiState`의 `isInProgress`(place)와 `defaultProvider`(setting)는 `PlaceAddViewModel`이 `combine`으로 만든다.

나눈 ViewModel은 서로 참조하지 않는다. 저장처럼 다른 관심사의 값이 필요한 동작은 그 값을 파라미터로 받는다.

⚠️ 비권장 예시:

```kotlin
// 메모 목록, 필터 표시, 동기화 진행이 한 클래스에 모여 있다.
internal class MemoHomeViewModel(
    pageMemoHomeUseCase: PageMemoHomeUseCase,
    getMemoFilterUseCase: GetMemoFilterUseCase,
    getProgressReportedUseCase: GetProgressReportedUseCase,
    private val requestSyncUseCase: RequestSyncUseCase,
    // ...
) : ViewModel()
```

✅ 권장 예시:

```kotlin
internal class MemoHomeViewModel(
    pageMemoHomeUseCase: PageMemoHomeUseCase,
    getMemoFilterUseCase: GetMemoFilterUseCase,
) : ViewModel()

// 동기화 진행은 메모 목록과 따로 둔다.
internal class MemoHomeSyncViewModel(
    getProgressReportedUseCase: GetProgressReportedUseCase,
    private val requestSyncUseCase: RequestSyncUseCase,
) : ViewModel()
```

## UiState 소유

**UiState를 만드는 것은 ViewModel의 일이다.** ViewModel은 UseCase가 주는 값을 조합해 `StateFlow<XxxUiState>`로 노출하고, 컴포저블은 그 UiState를 받아 표시만 한다. 컴포저블에 `Boolean`이나 도메인 모델을 그대로 넘겨 화면이 UiState를 다시 만들게 하지 않는다. 표시 여부 판단(`isMapDisplayed = provider != null` 같은 것)이 UiState 밖으로 새면 화면마다 흩어진다.

**컴포저블은 UiState를 만들지 않고 조합한다.** Screen → Scaffold → Component로 내려가며 여러 ViewModel의 UiState를 나란히 받아 배치한다. 한 컴포넌트가 여러 UiState를 함께 읽어야 하면 `MemoPlaceCardUiState`처럼 그 UiState들을 담는 표현 단위를 두고 화면이 묶어 넘긴다.

예외는 **어느 ViewModel도 혼자 만들 수 없는 값**뿐이다. `CalendarHomeScaffoldUiState.isRefreshing`은 동기화·공휴일·날씨 세 ViewModel의 진행 상태를 합친 값이라 화면이 계산한다. ViewModel 하나가 만들 수 있는데도 화면에서 만들고 있다면 잘못 나눈 것이다.

⚠️ 비권장 예시:

```kotlin
internal class MemoHomeSyncViewModel(...) : ViewModel() {
    val isRefreshing: StateFlow<Boolean> = ...
}

@Composable
internal fun MemoHomeScreen(syncViewModel: MemoHomeSyncViewModel) {
    val isRefreshing by syncViewModel.isRefreshing.collectAsStateWithLifecycle()

    MemoHomeScaffold(
        // 화면이 UiState를 만들고 있다.
        memoListUiStateProvider = { MemoListUiState(isRefreshing = isRefreshing) },
    )
}
```

## ViewModel 시작 트리거

ViewModel의 `init` 블록에서 UseCase 호출이나 Flow collect 같은 작업을 시작하지 않는다. `init`은 화면 라이프사이클과 무관하게 실행되어 시작 시점을 제어할 수 없고, 테스트에서 준비와 실행을 나눌 수 없다.

작업 시작은 항상 UI에서 트리거한다. 상황에 맞게 `LaunchedEffect`, `LifecycleEventEffect`·`LifecycleStartEffect`·`LifecycleResumeEffect` 같은 lifecycle 효과, retain effect를 사용한다. 트리거는 재진입마다 반복 호출될 수 있으므로 ViewModel의 시작 함수는 이미 시작했거나 끝난 작업을 다시 시작하지 않게 만든다.

**재시작을 막는 판단은 그 작업을 소유한 곳에 하나만 둔다.** 시작 함수를 다시 부르면 작업이 실제로 다시 시작되는 경우에만 ViewModel이 상태로 막는다. 원격을 다시 호출하는 조회가 그렇다.

**아래 계층이 멱등한 연산을 위임받는 시작 함수에는 가드를 두지 않는다.** ViewModel 플래그를 더하면 같은 보장이 두 곳으로 갈라지고, 아래 계층의 멱등성이 깨져도 ViewModel 테스트가 회귀를 알려주지 못한다. 예: 주기 동기화 예약은 실행 수단이 예약을 하나로 유지하므로([work.md](work.md)의 `예약은 하나로 유지한다`) `AppSyncViewModel.schedulePeriodicSync()`는 요청을 삼키지 않고 UseCase를 그대로 호출한다.

지속 관찰하는 상태는 `init`에서 collect하는 대신 `stateIn(started = SharingStarted.WhileSubscribed(...))`처럼 구독자가 있을 때만 collect되는 형태로 노출한다.

⚠️ 비권장 예시:

```kotlin
internal class HolidayHomeYearViewModel(...) : ViewModel() {
    init {
        fetch()
    }
}
```

✅ 권장 예시:

```kotlin
internal class HolidayHomeYearViewModel(...) : ViewModel() {
    fun fetch() {
        // 화면 재진입마다 호출될 수 있으므로 아직 시작하지 않은 경우에만 동기화한다.
        if (fetchState.value != FetchState.NONE) return

        startFetch()
    }
}

@Composable
private fun FetchHolidayEffect(viewModel: HolidayHomeYearViewModel) {
    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        viewModel.fetch()
    }
}
```
