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

## 노출하는 Flow

**ViewModel 안에서는** UseCase 결과와 `combine` 결과를 cold Flow, `SharedFlow`, `StateFlow` 어느 것으로든 다룬다.

**ViewModel 밖(컴포저블)으로 노출하는 값은 읽기 전용 `StateFlow`다.** 구독하는 즉시 현재 값을 받고, 같은 값을 다시 보내지 않으며, 구독할 때마다 원천을 다시 수집하지 않는다. 화면이 그리는 상태뿐 아니라 값이 바뀔 때 작업을 실행하는 트리거도 같다. 값의 타입은 `UiState 소유` 절대로 `XxxUiState`이고, 변경 가능한 타입(`MutableStateFlow`, `MutableSharedFlow`, `Channel`)은 노출하지 않는다.

**`stateIn`의 초기값은 스펙상 안전한 기본값으로 둔다.** 확인 전의 계정을 게스트가 아닌 것으로 보는 `FileAddAccountUiState(isGuest = false)`처럼, 그 값으로 화면을 그리거나 작업을 실행해도 스펙과 어긋나지 않아야 한다. 안전한 기본값이 없으면 `T?`의 `null`로 대신하지 않고 sealed UiState의 `Loading`으로 드러낸다. `null`은 "아직 확인하지 못함"과 "확인했지만 없음"을 구분하지 못하고, 받는 쪽마다 그 의미를 다시 해석하게 만든다.

`filterNotNull()`처럼 값을 걸러 초기값 문제를 피하지 않는다. `stateIn`은 어차피 초기값을 요구하고, 걸러낸 상태가 다음 값과 같으면 중복 제거로 그 전환이 사라진다. ViewModel 안에서 거르는 것은 실패한 `Result`처럼 상태가 아닌 값에 한한다.

예외는 둘뿐이다.

- **`cachedIn(viewModelScope)`을 적용한 `Flow<PagingData<T>>`.** `PagingData`는 한 번만 수집할 수 있어 `stateIn`·`shareIn`으로 감싸지 않는다.
- **`Channel`을 원천으로 하는 일회성 이벤트(`Flow<XxxEffect>`).** ViewModel이 가진 `Channel`의 `receiveAsFlow()`, 아래 계층이 가진 `Channel`을 변환한 Flow가 해당한다.

상태를 이벤트처럼 노출하지 않는다. 저장소의 값을 `mapNotNull`로 걸러 `Flow<XxxEffect>`로 내보내면 구독할 때마다 원천을 다시 수집하고, 같은 상태가 다시 수집될 때마다 이벤트가 반복된다.

⚠️ 비권장 예시:

```kotlin
internal class FileAddAccountViewModel(getAccountUseCase: GetAccountUseCase) : ViewModel() {
    // 계정 상태를 이벤트처럼 내보낸다.
    val effect: Flow<FileAddAccountEffect> =
        getAccountUseCase(parameter = Unit).mapNotNull { result ->
            if (result.getOrNull() is Account.Guest) FileAddAccountEffect.BecameGuest else null
        }
}

internal class AppSyncViewModel(...) : ViewModel() {
    // 트리거를 StateFlow가 아닌 Flow로 노출한다.
    val authenticatedAccountId: Flow<Uuid> = ...shareIn(scope = viewModelScope, started = ..., replay = 1)

    // null이 "확인 전"과 "인증되지 않음"을 겹쳐 표현한다.
    val authenticatedAccountId: StateFlow<Uuid?> = ...stateIn(scope = viewModelScope, started = ..., initialValue = null)
}
```

✅ 권장 예시:

```kotlin
internal class FileAddAccountViewModel(getAccountUseCase: GetAccountUseCase) : ViewModel() {
    val uiState: StateFlow<FileAddAccountUiState> =
        getAccountUseCase(parameter = Unit)
            .map { result -> FileAddAccountUiState(isGuest = result.getOrNull() is Account.Guest) }
            .stateIn(scope = viewModelScope, started = SharingStarted.WhileUiSubscribed, initialValue = FileAddAccountUiState())
}

internal sealed interface AppSyncUiState {
    data object Loading : AppSyncUiState

    data object Unauthenticated : AppSyncUiState

    data class Authenticated(val accountId: Uuid) : AppSyncUiState
}

internal class AppSyncViewModel(...) : ViewModel() {
    val uiState: StateFlow<AppSyncUiState> = ...stateIn(scope = viewModelScope, started = ..., initialValue = AppSyncUiState.Loading)
}

@Composable
internal fun SyncEffect(requestSync: () -> Unit, uiState: Flow<AppSyncUiState> = emptyFlow()) {
    CollectEffect(effect = uiState) { value ->
        if (value is AppSyncUiState.Authenticated) requestSync()
    }
}
```

## 페이징 조회 실패

`Flow<Result<PagingData<T>>>`의 실패는 ViewModel이 `PagingData.empty()`로 바꾼다. 마지막 성공 목록을 붙잡아 두지 않는다(`runningFold`·`filterNotNull`·`mapNotNull`로 실패를 건너뛰지 않는다).

이어서 불러오기의 실패는 `PagingSource`가 `LoadResult.Error`로 돌려주고 Paging이 이미 불러온 항목을 유지하므로 ViewModel이 관여하지 않는다. ViewModel에 실패로 도달하는 것은 계정 조회처럼 페이징 원천 자체의 실패다.

```kotlin
val memoPagingData: Flow<PagingData<Memo>> =
    pageMemoUseCase(parameter = sort)
        .map { result -> result.getOrElse { PagingData.empty() } }
        .cachedIn(viewModelScope)
```

## ViewModel 시작 트리거

ViewModel의 `init` 블록에서 UseCase 호출이나 Flow collect 같은 작업을 시작하지 않는다. `init`은 화면 라이프사이클과 무관하게 실행되어 시작 시점을 제어할 수 없고, 테스트에서 준비와 실행을 나눌 수 없다.

작업 시작은 항상 UI에서 트리거한다. 상황에 맞게 `LaunchedEffect`, `LifecycleEventEffect`·`LifecycleStartEffect`·`LifecycleResumeEffect` 같은 lifecycle 효과, retain effect를 사용한다. 트리거는 재진입마다 반복 호출될 수 있으므로 시작 함수에는 `UseCase 호출 가드`를 둔다.

지속 관찰하는 상태는 `init`에서 collect하는 대신 `stateIn(started = SharingStarted.WhileUiSubscribed)`처럼 구독자가 있을 때만 collect되는 형태로 노출한다. 구독이 끊긴 뒤 값을 초기값으로 되돌려야 하는 `stateIn`에서만 `SharingStarted.WhileSubscribed(..., replayExpirationMillis = 0)`을 직접 쓴다.

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

## UseCase 호출 가드

**UseCase를 호출하는 ViewModel 함수는 예외 없이 중복 실행을 막는 가드를 둔다.** 조회(`fetch`·`refresh`), 저장, 삭제, 예약 요청 모두 대상이다. 화면 재진입, 연속 클릭, 재구성으로 같은 함수가 다시 불릴 수 있고, 아래 계층이 멱등한지는 ViewModel이 기대지 않는다.

가드는 함수 첫 줄에서 판단하고 해당하면 바로 반환한다. 판단 수단은 다음 중 그 함수에 맞는 것을 고른다.

- UiState나 진행 상태가 이미 그 작업의 상태를 담고 있으면 그 값을 확인한다. 예: `if (fetchState.value != FetchState.NONE) return`
- 진행 중 여부만 필요하면 `Boolean` 상태를 둔다. 예: `if (isInProgress.value) return`을 두고 `try`/`finally`로 해제한다.
- 한 번만 실행하면 되는 시작 함수는 시작 여부를 `Boolean`으로 기억한다.
- 대상마다 따로 막아야 하면 진행 중인 대상의 집합을 둔다. 예: 메모별 완료·삭제.

가드 플래그는 ViewModel 안에서만 바꾸고, 화면에 보여야 하면 UiState로 노출한다.

계기마다 다시 실행되어야 하는 함수(앱 복귀, 계정 변경마다 부르는 예약·동기화 요청)는 한 번 실행했다는 사실을 영구히 기억하지 않는다. 진행 중에만 막고 끝나면 다시 받는다. 영구 플래그는 다음 계기의 요청까지 삼킨다.

⚠️ 비권장 예시:

```kotlin
internal class AppSyncViewModel(...) : ViewModel() {
    fun requestSync(trigger: SyncTrigger) {
        viewModelScope.launch { requestSyncUseCase(parameter = trigger) }
    }
}
```

✅ 권장 예시:

```kotlin
internal class AppSyncViewModel(...) : ViewModel() {
    private var isRequesting = false

    fun requestSync(trigger: SyncTrigger) {
        if (isRequesting) return
        isRequesting = true

        viewModelScope.launch {
            try {
                requestSyncUseCase(parameter = trigger)
            } finally {
                isRequesting = false
            }
        }
    }
}
```
