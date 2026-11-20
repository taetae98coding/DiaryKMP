# ViewModel 규칙

## 화면당 ViewModel 분리

**한 화면에 ViewModel을 하나만 두지 않고, 관심사마다 나눈다.** 하나로 두면 관계없는 상태가 한 `combine`에 묶여 함께 다시 계산되고, 테스트가 나머지 의존성까지 mock으로 채워야 한다.

**나누는 단위는 UiState다.** 하나의 UiState를 만드는 데 필요한 값은 한 ViewModel이 모아서 조합하고, UiState가 다르면 ViewModel을 나눈다. 아래 기준은 UiState를 나눌지 판단할 때 함께 본다.

- **시작 트리거가 다르면 나눈다.** 화면 진입에 시작하는 조회와 사용자가 눌러야 시작하는 동작은 같은 ViewModel에 두지 않는다.
- **화면에서 사라져도 나머지가 그대로면 나눈다.** 동기화 진행 표시, 필터 적용 표시처럼 곁들여진 상태가 여기 해당한다. 예: MemoHome은 목록의 `MemoHomeViewModel`과 동기화 진행의 `SyncRefreshViewModel`을 따로 받는다.
- **같은 대상의 CRUD는 나누지 않는다.** 한 메모의 조회·수정·완료·복사·삭제처럼 대상이 하나이고 동작이 그 대상의 수명주기를 이루면 한 ViewModel에 둔다.

**서로 다른 도메인에서 온다는 이유만으로는 나누지 않는다.** 같은 UiState의 필드로 함께 표시된다면 그 조합은 ViewModel의 일이다. 예: `PlaceAddUiState`의 `isInProgress`(place)와 `defaultProvider`(setting)는 `PlaceAddViewModel`이 `combine`으로 만든다.

나눈 ViewModel은 서로 참조하지 않는다. 저장처럼 다른 관심사의 값이 필요한 동작은 그 값을 파라미터로 받는다.

## ViewModel 상속 대신 합성

**ViewModel은 androidx `ViewModel`만 상속한다.** 여러 ViewModel이 함께 쓰는 로직은 추상·open ViewModel로 올리지 않고, 필요한 UseCase를 생성자로 받는 일반 상태 홀더 클래스(`feature:core`의 `MemoListState`, `ListItemActions`, `ListItemDeleteActions`, `TagFilterState`, `RoutinePagingState`)로 뽑아 각 ViewModel이 `private` 필드로 합성한다. ViewModel은 화면에 필요한 값과 함수만 홀더에 위임해 노출한다. 상속하면 한 화면의 동작이 부모와 자식 여러 파일에 흩어지고, 부모를 바꾸면 모든 자식에 전파되며, 테스트가 계층 전체를 알아야 한다.

**홀더는 `CoroutineScope`를 받지 않는다.** Flow 변환과 `suspend` 함수만 내보내고, `launch`·`stateIn`·`cachedIn`은 ViewModel이 `viewModelScope`로 적용한다([main-safety.md](main-safety.md)의 `그 밖의 작업은 suspend로 내보내고 호출자가 scope를 정한다`). 홀더의 `suspend` 함수에 둔 `UseCase 호출 가드`는 함수 첫 줄에서 판단하고 `try`·`finally`로 해제한다.

**구성 차이가 상수 하나면 하위 클래스를 만들지 않고 `@InjectedParam` 파라미터로 넘긴다**(`EntityDetailTagViewModel`은 `koinViewModel { parametersOf(EntityTagKind.CONTACT, key.id) }`로 종류를 받는다). 여러 화면이 같은 ViewModel 타입을 받아야 하면 공통 인터페이스를 두고 각 ViewModel이 구현한다.

```kotlin
@KoinViewModel
internal class MemoFinishedListViewModel(pageFinishedMemoUseCase: PageFinishedMemoUseCase, ...) : ViewModel() {
    private val memoListState = MemoListState(pageMemo = { sort -> pageFinishedMemoUseCase(parameter = sort) })
    private val memoActions = ListItemActions(...)

    val memoPagingData: Flow<PagingData<MemoListItem>> = memoListState.memoPagingData.cachedIn(viewModelScope)

    fun finish(id: Uuid) {
        viewModelScope.launch { memoActions.finish(id = id) }
    }
}
```

## UiState 소유

**UiState를 만드는 것은 ViewModel의 일이다.** ViewModel은 UseCase가 주는 값을 조합해 `StateFlow<XxxUiState>`로 노출하고, 컴포저블은 그 UiState를 받아 표시만 한다. 컴포저블에 `Boolean`이나 도메인 모델을 그대로 넘겨 화면이 UiState를 다시 만들게 하지 않는다(`StateFlow<Boolean>`을 수집해 화면에서 `XxxUiState(isRefreshing = ...)`로 감싸지 않는다). 표시 여부 판단(`isMapDisplayed = provider != null` 같은 것)이 UiState 밖으로 새면 화면마다 흩어진다.

**컴포저블은 UiState를 만들지 않고 조합한다.** Screen → Scaffold → Component로 내려가며 여러 ViewModel의 UiState를 나란히 받아 배치한다. 한 컴포넌트가 여러 UiState를 함께 읽어야 하면 `MemoPlaceCardUiState`처럼 그 UiState들을 담는 표현 단위를 두고 화면이 묶어 넘긴다.

예외는 **어느 ViewModel도 혼자 만들 수 없는 값**뿐이다. `CalendarHomeScaffoldUiState.isRefreshing`은 동기화·공휴일·날씨 세 ViewModel의 진행 상태를 합친 값이라 화면이 계산한다. ViewModel 하나가 만들 수 있는데도 화면에서 만들고 있다면 잘못 나눈 것이다.

## 노출하는 Flow

**ViewModel 밖(컴포저블)으로 노출하는 값은 읽기 전용 `StateFlow<XxxUiState>`다.** 화면이 그리는 상태뿐 아니라 값이 바뀔 때 작업을 실행하는 트리거도 같다. ViewModel 안에서 쓰는 `private` 값은 cold Flow, `SharedFlow`, `StateFlow` 어느 것이든 된다.

**`stateIn`의 초기값은 스펙상 안전한 기본값으로 둔다.** 확인 전의 계정을 게스트가 아닌 것으로 보는 `FileAddAccountUiState(isGuest = false)`처럼, 그 값으로 화면을 그리거나 작업을 실행해도 스펙과 어긋나지 않아야 한다. 안전한 기본값이 없으면 `T?`의 `null`로 대신하지 않고 sealed UiState의 `Loading`으로 드러낸다. `null`은 "아직 확인하지 못함"과 "확인했지만 없음"을 구분하지 못하고, 받는 쪽마다 그 의미를 다시 해석하게 만든다.

`filterNotNull()`처럼 값을 걸러 초기값 문제를 피하지 않는다. `stateIn`은 어차피 초기값을 요구하고, 걸러낸 상태가 다음 값과 같으면 중복 제거로 그 전환이 사라진다. ViewModel 안에서 거르는 것은 실패한 `Result`처럼 상태가 아닌 값에 한한다.

예외는 둘뿐이다.

- **`cachedIn(viewModelScope)`을 적용한 `Flow<PagingData<T>>`.** `PagingData`는 한 번만 수집할 수 있어 `stateIn`·`shareIn`으로 감싸지 않는다.
- **`Channel`을 원천으로 하는 일회성 이벤트(`Flow<XxxEffect>`).** ViewModel이 가진 `Channel`의 `receiveAsFlow()`, 아래 계층이 가진 `Channel`을 변환한 Flow가 해당한다.

**Effect에는 유실돼도 화면 상태가 어긋나지 않는 일회성 안내와 이동만 싣는다.** 스낵바 안내, 입력란 포커스 요청, 화면 닫기가 여기 속한다(`WebDetailEffect`). 유실되면 화면이 틀린 상태로 남는 결과는 Effect가 아니라 UiState에 둔다. Android 공식 가이드는 ViewModel이 UI로 이벤트를 보내지 않고 결과를 UiState에 두도록 권장하지만([UI events](https://developer.android.com/topic/architecture/ui-layer/events)), 이 저장소는 `Channel` Effect를 택했으므로 이 항목에서는 `참고 우선순위`보다 이 문서를 우선한다.

상태를 이벤트처럼 노출하지 않는다. 저장소의 값을 `mapNotNull`로 걸러 `Flow<XxxEffect>`로 내보내면 구독할 때마다 원천을 다시 수집하고, 같은 상태가 다시 수집될 때마다 이벤트가 반복된다. 상태 변화에 반응해야 하면 UiState로 노출하고 UI가 `CollectEffect`로 받아 구분한다([compose.md](compose.md)의 `UI Effect 수집`).

```kotlin
// Uuid?의 null이 "확인 전"과 "인증되지 않음"을 겹쳐 표현하지 않도록 sealed UiState로 나눈다.
internal sealed interface AppSyncUiState {
    data object Loading : AppSyncUiState

    data object Unauthenticated : AppSyncUiState

    data class Authenticated(val accountId: Uuid) : AppSyncUiState
}

@Composable
internal fun SyncEffect(requestSync: () -> Unit, uiState: Flow<AppSyncUiState> = emptyFlow()) {
    CollectEffect(effect = uiState) { value ->
        if (value is AppSyncUiState.Authenticated) requestSync()
    }
}
```

## 페이징 조회 실패

`Flow<Result<PagingData<T>>>`의 실패는 ViewModel이 `PagingData.empty()`로 바꾼다(`result.getOrElse { PagingData.empty() }`). 마지막 성공 목록을 붙잡아 두지 않는다(`runningFold`·`filterNotNull`·`mapNotNull`로 실패를 건너뛰지 않는다).

조회를 마쳤는지로 다음 동작을 가르는 흐름(대상이 없으면 추가 화면으로 보내는 판정 등)은 예외다. 조회 상태를 싣지 않은 `PagingData.empty()`는 `cachedIn`을 다시 수집하는 화면에 조회를 마친 빈 목록으로 전달되므로, 이런 흐름은 실패를 refresh `LoadState.Error`를 실은 빈 `PagingData`로 바꾼다.

불러오기 상태를 싣지 않은 `PagingData.empty()`를 처음 받은 목록은 처음의 불러오는 중에 머문다. 그래서 불러오는 중과 빈 목록을 불러오기 상태로 구분해 그리는 목록(루틴, 디데이)은 실패를 조회를 마친 상태(`LOADED_EMPTY_LOAD_STATES`)를 실은 빈 `PagingData`로 바꿔 빈 상태를 표시하게 한다. 반대로 조회 조건(오늘 날짜)이 정해지기 전에는 상태를 싣지 않은 `PagingData.empty()`를 내보내 불러오는 중으로 둔다.

이어서 불러오기의 실패는 `PagingSource`가 `LoadResult.Error`로 돌려주고 Paging이 이미 불러온 항목을 유지하므로 ViewModel이 관여하지 않는다. ViewModel에 실패로 도달하는 것은 계정 조회처럼 페이징 원천 자체의 실패다.

## ViewModel 시작 트리거

ViewModel의 `init` 블록에서 UseCase 호출이나 Flow collect 같은 작업을 시작하지 않는다. `init`은 화면 라이프사이클과 무관하게 실행되어 시작 시점을 제어할 수 없고, 테스트에서 준비와 실행을 나눌 수 없다.

작업 시작은 항상 UI의 Effect 컴포저블이 트리거한다(`FetchHolidayEffect`가 `LifecycleEventEffect(Lifecycle.Event.ON_START) { viewModel.fetch() }`로 부른다). 수단은 [compose.md](compose.md)의 `생명주기는 호출자가 소유한다`에서 고른다. 트리거는 재진입마다 반복 호출될 수 있으므로 시작 함수에는 `UseCase 호출 가드`를 둔다.

지속 관찰하는 상태는 `init`에서 collect하는 대신 `stateIn(started = SharingStarted.WhileUiSubscribed)`(`library:coroutines`)로 노출한다. 구독이 끊긴 뒤 값을 초기값으로 되돌려야 하는 `stateIn`에서만 `SharingStarted.WhileSubscribed(stopTimeoutMillis = UI_STOP_TIMEOUT_MILLIS, replayExpirationMillis = 0)`을 직접 쓴다. 예: `AppSyncViewModel`은 같은 계정으로 다시 인증될 때도 값이 다시 바뀌어야 트리거가 실행된다.

**스펙이 화면이 가려진 동안에도 이어져야 한다고 정한 관찰은 `stateIn`으로 노출하지 않고, Effect가 트리거한 시작 함수가 `viewModelScope`에서 한 번만 collect한다.** `stateIn(WhileUiSubscribed)`는 구독이 끊기면 원천 수집을 멈추고 돌아오면 처음부터 다시 수집하므로, 이미 받은 결과를 다시 요청하게 된다. 예: `WebDetailPageViewModel.load()`는 저장된 URL과 요청 헤더가 바뀔 때만 웹 페이지를 다시 불러와야 하고 백그라운드에서 돌아와도 다시 불러오지 않아야 하므로([WebDetail 화면 스펙](../../docs/spec/client/web-detail.md)의 `불러오기 시점`), 시작 여부를 `Boolean`으로 기억하고 한 번만 collect한다.

## UseCase 호출 가드

**UseCase를 호출하는 ViewModel 함수는 예외 없이 중복 실행을 막는 가드를 둔다.** 조회(`fetch`·`refresh`), 저장, 삭제, 예약 요청 모두 대상이다. 화면 재진입, 연속 클릭, 재구성으로 같은 함수가 다시 불릴 수 있고, 아래 계층이 멱등한지는 ViewModel이 기대지 않는다.

가드는 함수 첫 줄에서 판단하고 해당하면 바로 반환한다. 판단 수단은 다음 중 그 함수에 맞는 것을 고른다.

- UiState나 진행 상태가 이미 그 작업의 상태를 담고 있으면 그 값을 확인한다.
- 진행 중 여부만 필요하면 `Boolean` 상태를 둔다. `if (isInProgress) return`을 두고 `try`/`finally`로 해제한다.
- 한 번만 실행하면 되는 시작 함수는 시작 여부를 `Boolean`으로 기억한다.
- 대상마다 따로 막아야 하면 진행 중인 대상의 집합을 둔다. 예: 메모별 완료·삭제(`ListItemActions`).

가드 플래그는 ViewModel 안에서만 바꾸고, 화면에 보여야 하면 UiState로 노출한다.

계기마다 다시 실행되어야 하는 함수(앱 복귀, 계정 변경마다 부르는 예약·동기화 요청, 화면에 다시 드러날 때마다 하는 조회)는 한 번 실행했다는 사실을 영구히 기억하지 않는다. 진행 중에만 막고 끝나면 다시 받는다. 영구 플래그는 다음 계기의 요청까지 삼킨다.

## 프로세스 종료 뒤에도 남는 상태

**프로세스 종료 뒤에도 남아야 하는 ViewModel 소유 상태만 `SavedStateHandle`에 두고, `getMutableStateFlow(key, 초기값)`으로 얻은 Flow 하나를 읽고 쓴다.** `savedStateHandle[key]`와 `MutableStateFlow`를 따로 두고 함께 쓰면 한쪽만 바꾼 자리에서 화면과 복원 값이 어긋난다. UiState는 그 Flow를 `map`·`stateIn`으로 바꿔 노출하고 초기값에 Flow의 현재 값을 넣는다(`MoreHomeSignOutViewModel`). 진행 중 여부처럼 되살릴 때 초기값으로 돌아가야 하는 상태는 넣지 않는다.

**저장하는 값이 화면에 보이는 값과 달라야 할 때만 `savedStateHandle[key]`에 직접 쓴다.** `ProfileDeleteViewModel`은 Apple 재인증 중 다이얼로그를 화면에는 열어 두고 복원 값은 닫힘으로 둔다. `getMutableStateFlow`의 Flow는 `savedStateHandle[key]`에 쓴 값도 그대로 따라가므로 두 값을 다르게 둘 수 없다.

화면 인자는 `SavedStateHandle`에서 읽지 않고 `@InjectedParam`으로 받는다.
