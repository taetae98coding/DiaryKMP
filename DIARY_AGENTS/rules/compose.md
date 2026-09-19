# Compose UI 규칙

## 파라미터 기본값과 선언 순서

컴포저블의 파라미터는 계층에 따라 기본값 정책이 다르다.

**동작에 필수인 파라미터에는 어느 계층에서도 기본값을 두지 않는다.** 콜백은 컴포저블이 하는 일 그 자체이므로 `onClick: () -> Unit = {}`처럼 no-op 기본값을 두면 호출부가 콜백을 빠뜨려도 컴파일되고, 아무 반응이 없는 화면이 컴파일러에 걸리지 않는다. 반환 타입이 `Unit`인 콜백(`onEvent`, `onClick`, `navigateUp`)은 필수로 둔다. nullable 콜백은 없어도 되는 상호작용을 타입으로 이미 표현하고 있으므로 `null`을 기본값으로 둔다.

**Screen 컴포저블은 `modifier` 외에 기본값을 두지 않는다.** Screen은 내비게이션 진입점이 조립하는 화면 단위이므로, ViewModel·state·표시 조건 같은 의존성을 호출부가 전부 명시하게 한다. 기본값을 두면 진입점마다 무엇이 주입되는지가 선언에서 사라지고, `koinViewModel()`처럼 호출부 문맥이 필요한 값이 컴포저블 안에 숨는다. `modifier`는 화면을 배치하는 호출부의 관심사이고 주입 의존성이 아니므로 다른 계층과 같이 `Modifier`를 기본값으로 둔다.

**Scaffold 이하 컴포저블은 표현에 관한 파라미터에 모두 기본값을 둔다.** 이 계층은 재사용하는 표현 단위이므로 호출자가 관심 있는 것만 넘기게 한다.

| 파라미터 | 기본값 |
| --- | --- |
| `modifier` | `Modifier` |
| `rememberXxx()` 팩토리가 있는 state 홀더 | 그 팩토리 호출 |
| UI state provider | 기본값으로 생성한 state를 반환하는 람다 |
| sealed UI state provider | 초기 상태(`Loading`, `Idle`)를 반환하는 람다 |
| nullable 콜백 | `null` |
| `Boolean` 플래그 | 비활성·기본 표현에 해당하는 값 |
| 목록·`Flow`·`LazyPagingItems` | 비어 있는 값 |

기본값으로 표현할 수 없는 도메인 데이터(식별자, 모델, 조회 결과)와 ViewModel은 필수로 남긴다. ViewModel은 어느 계층에서든 호출부가 명시해야 주입 경로가 드러난다. 내용을 채우는 `@Composable` trailing 슬롯도 필수로 남긴다. 빈 슬롯을 기본값으로 두면 아무것도 그리지 않는 컴포저블이 호출자에게 유효한 사용처럼 보인다.

`expect` 컴포저블은 `expect` 선언에만 기본값을 두고, `actual` 선언은 기본값 없이 같은 파라미터 순서를 따른다.

**선언 순서는 `필수 파라미터` → `modifier` → `나머지 선택 파라미터` → `trailing 슬롯`이다.** `modifier`는 선택 파라미터 중 가장 앞에 두어 호출부에서 항상 같은 자리에 오게 하고, trailing 슬롯은 후행 람다 문법을 쓸 수 있도록 마지막에 둔다. Screen은 `modifier` 말고 선택 파라미터가 없으므로 `modifier`가 마지막에 온다.

**호출부는 기본값과 같은 값을 다시 넘기지 않는다.** 특히 preview는 기본값으로 충분한 파라미터를 생략하고, 그 preview가 보여주려는 상태만 넘긴다.

✅ 권장 예시:

```kotlin
@Composable
internal fun MemoHomeScreen(
    navigateToAdd: () -> Unit,
    navigateToDetail: (Uuid) -> Unit,
    componentVisibleProvider: () -> MemoHomeScaffoldComponentVisible,
    memoViewModel: MemoHomeViewModel,
    syncViewModel: MemoHomeSyncViewModel,
    modifier: Modifier = Modifier,
)

@Composable
internal fun MemoHomeScaffold(
    onEvent: (MemoHomeScaffoldEvent) -> Unit,
    onMemoListEvent: (MemoListEvent) -> Unit,
    modifier: Modifier = Modifier,
    memoListState: MemoListState = rememberMemoListState(),
    memoPagingItems: LazyPagingItems<MemoListItem> = remember { flowOf(PagingData.empty<MemoListItem>()) }.collectAsLazyPagingItems(),
    memoListUiStateProvider: () -> MemoListUiState = { MemoListUiState() },
)

@ScreenPreview
@Composable
private fun MemoHomeScaffoldPreview() {
    DiaryTheme {
        MemoHomeScaffold(
            onEvent = {},
            onMemoListEvent = {},
        )
    }
}
```

## state 읽기 지연

**상위 컴포저블 본문에서 state를 읽어 값으로 내려보내지 않고, 읽지 않은 채로 넘겨 그 값을 실제로 쓰는 컴포저블에서 읽는다.** Compose는 state를 읽은 컴포저블을 recomposition 대상으로 삼으므로, 상위에서 읽으면 그 값과 무관한 형제까지 함께 다시 구성된다. state 홀더나 람다로 넘기면 읽는 시점이 호출된 컴포저블 안으로 미뤄져 recomposition 범위가 그 컴포저블로 좁아진다.

- 판단 기준: https://developer.android.com/develop/ui/compose/performance/bestpractices#defer-reads

대상은 `mutableStateOf` 프로퍼티, `collectAsStateWithLifecycle`로 얻은 값, state 홀더의 프로퍼티처럼 읽으면 구독이 생기는 값 전부다. 상수와 이미 읽은 값을 가공한 결과는 대상이 아니다.

**미루는 방법은 state 홀더를 넘기는 것과 `xxxProvider`를 넘기는 것 둘이고, 홀더를 넘길 수 있으면 홀더를 넘긴다.** 쓰는 값이 하나여도 그렇게 한다. 값 개수로 방식을 가르면 쓰는 값이 하나에서 둘로 늘 때 시그니처를 함께 고쳐야 하고, 같은 화면의 컴포저블끼리 파라미터 모양이 달라진다. 홀더를 넘기면 파라미터가 하나로 고정되고, 호출부가 홀더를 프로퍼티별 람다로 조각내 다시 조립하지 않는다.

**`xxxProvider`는 홀더를 넘길 수 없을 때 쓴다.** 다음 셋이 그렇다.

- 그 홀더를 모르는 컴포저블. `compose:*`의 공용 컴포넌트는 여러 화면이 각자 다른 홀더로 쓰므로 특정 화면의 홀더 타입에 의존할 수 없다.
- 홀더에 없는 값. ViewModel이 노출해 `collectAsStateWithLifecycle`로 얻은 UI state와 거기에서 파생한 값이 그렇다.
- 홀더의 프로퍼티를 그대로 쓰지 않고 가공해 넘기는 값. 목록의 각 항목이 선택되었는지 같은 값이 그렇다.

홀더를 받은 컴포저블은 본문에서 필요한 프로퍼티만 읽는다. `xxxProvider`를 받은 컴포저블은 본문에서 한 번 호출해 지역 변수로 두고 쓴다. 호출부는 `xxxProvider = { state.value }`처럼 읽는 코드를 그대로 람다에 담고, 람다 밖에서 미리 읽지 않는다.

다음은 값으로 그대로 넘긴다.

- 그 값이 바뀌면 어차피 그 컴포저블 전체가 다시 그려져야 하는 경우. 예: `DiaryCrossfade`의 `targetState`, `when` 분기의 대상
- 호출부가 state가 아닌 상수나 파라미터를 넘기는 경우
- 그리기나 배치에만 쓰는 값. 이 경우는 컴포저블이 아니라 단계로 미루므로 `그리기·배치 단계로 미루기`를 따른다.

⚠️ 비권장 예시:

```kotlin
@Composable
private fun WideContent(
    state: WebDetailScaffoldState,
    uiStateProvider: () -> WebDetailUiState,
) {
    Row {
        WebDetailForm()
        // 표시 방식과 URL을 본문에서 읽으므로 둘 중 하나만 바뀌어도 형제인 폼까지 다시 구성된다.
        WebDetailPage(
            viewMode = state.viewMode,
            url = uiStateProvider().urlOrEmpty(),
        )
    }
}
```

✅ 권장 예시:

```kotlin
@Composable
private fun WideContent(
    state: WebDetailScaffoldState,
    uiStateProvider: () -> WebDetailUiState,
) {
    Row {
        WebDetailForm()
        WebDetailPage(
            // 표시 방식은 홀더가 소유하므로 홀더를 넘긴다.
            state = state,
            // 조회 결과의 URL은 어느 홀더에도 없으므로 provider로 넘긴다.
            urlProvider = { uiStateProvider().urlOrEmpty() },
        )
    }
}

@Composable
internal fun WebDetailPage(
    modifier: Modifier = Modifier,
    state: WebDetailScaffoldState = rememberWebDetailScaffoldState(),
    urlProvider: () -> String = { "" },
) {
    WebDetailViewModeBar(state = state)

    DiaryCrossfade(targetState = state.viewMode) { mode -> ... }
}
```

홀더를 넘겨도 호출부 본문에서는 아무 프로퍼티도 읽지 않아야 한다. 홀더를 넘기면서 그중 하나를 값으로도 함께 넘기면 읽기가 다시 위로 올라온다.

⚠️ 비권장 예시:

```kotlin
@Composable
internal fun WebDetailScaffold(state: WebDetailScaffoldState) {
    WebDetailViewModeBottomSheetHost(
        onEvent = onEvent,
        state = state,
        // 홀더를 넘기고도 표시 여부를 값으로 또 넘긴다. 이 한 줄 때문에 Scaffold가 다시 구성된다.
        isSheetVisible = state.viewModeSheetState.isVisible,
    )
}
```

이 규칙은 어디에서 읽을지를 정하고, 무엇을 읽을지는 `자주 바뀌는 state의 파생 값`이 정한다. 읽는 자리를 아래로 미뤄도 그 자리에서 프레임마다 바뀌는 값을 그대로 읽으면 recomposition 횟수는 줄지 않는다.

여기까지는 composition 안에서 읽는 자리를 옮기는 방법이다. 값을 그리기·배치에만 쓰거나 부수 효과의 계기로만 쓰면 아래 두 절에 따라 읽기를 composition 밖으로 옮긴다.

### 그리기·배치 단계로 미루기

**값을 그리기나 배치에만 쓰면 composition에서 읽지 않고 Modifier가 제공하는 람다 안에서 읽는다.** Compose는 composition, layout, draw 세 단계로 프레임을 만들고, 뒤 단계의 람다 안에서 읽은 state는 그 단계만 다시 실행시킨다. 같은 값을 composition에서 읽으면 자리만 달라지는 변화에도 세 단계를 모두 다시 거친다. 앞의 `xxxProvider`가 다시 구성할 컴포저블을 좁히는 것이라면, 이 방법은 아예 다시 구성하지 않게 한다.

- 판단 기준: https://developer.android.com/develop/ui/compose/phases#state-reads

같은 일을 값 오버로드와 람다 오버로드로 함께 제공하는 API는 람다 쪽을 쓴다.

| 쓸 API | 대신하는 것 | 읽는 단계 |
| --- | --- | --- |
| `Modifier.offset { }` | `Modifier.offset(x, y)` | 배치 |
| `Modifier.layout { }` | 크기·위치를 값으로 받는 Modifier | 배치 |
| `Modifier.graphicsLayer { }` | `Modifier.graphicsLayer(alpha = ...)` | 그리기 |
| `Modifier.drawBehind { }`, `Modifier.drawWithContent { }` | 배경·전경을 값으로 받는 Modifier | 그리기 |

**Modifier 확장 함수를 만들 때는 읽은 값이 아니라 state 홀더를 파라미터로 받는다.** 값으로 받으면 호출부가 composition에서 읽게 되어 람다 안에서 읽는 이득이 사라진다. 색이나 크기처럼 state가 아닌 값은 그대로 값으로 받는다.

⚠️ 비권장 예시:

```kotlin
// 호출부가 composition에서 읽으므로 선택 범위가 바뀌면 그 주 전체가 다시 구성된다.
modifier.calendarSelectBackground(
    yearMonth = yearMonth,
    weekOfMonth = weekOfMonth,
    dateRange = selectState.dateRange,
    color = CalendarDefault.selectBackgroundColor(),
)

internal fun Modifier.calendarSelectBackground(
    yearMonth: YearMonth,
    weekOfMonth: Int,
    dateRange: LocalDateRange?,
    color: Color,
): Modifier =
    drawBehind {
        if (dateRange == null) return@drawBehind
        drawRect(color = color, topLeft = ..., size = ...)
    }
```

✅ 권장 예시:

```kotlin
modifier.calendarSelectBackground(
    yearMonth = yearMonth,
    weekOfMonth = weekOfMonth,
    state = selectState,
    color = CalendarDefault.selectBackgroundColor(),
)

// 선택 범위를 그리기 단계에서 읽으므로 선택이 바뀌어도 다시 그리기만 한다.
internal fun Modifier.calendarSelectBackground(
    yearMonth: YearMonth,
    weekOfMonth: Int,
    state: CalendarSelectState,
    color: Color,
): Modifier =
    drawBehind {
        val dateRange = state.dateRange ?: return@drawBehind
        drawRect(color = color, topLeft = ..., size = ...)
    }
```

다음은 composition에서 읽는다. 그리기 자리만 달라지는 것이 아니면 recomposition을 피할 수 없다.

- 값이 자식의 내용이나 구조를 바꾸는 경우. 표시할 항목을 고르거나 분기를 가르는 값이 그렇다.
- 값으로 다른 컴포저블을 호출하거나 문자열 리소스를 고르는 경우.

레이아웃 결과를 state에 기록해 다른 컴포저블의 크기나 배치를 정하는 경우는 `Recomposition Loop (Cyclic Phase Dependency) 금지`를 따른다. 그 절이 부득이할 때의 완화책으로 드는 `Modifier.layout`과 `Modifier.offset { }`이 여기의 API와 같은 것이다.

### 부수 효과는 코루틴에서 읽기

**state 변화를 계기로 부수 효과를 실행할 때는 그 값을 composition에서 읽어 `LaunchedEffect`의 key로 넘기지 않고, `snapshotFlow`로 코루틴 안에서 읽는다.** key로 넘기려면 Effect 컴포저블이 그 값을 composition에서 읽어야 하므로, 값이 바뀔 때마다 Effect가 다시 구성되고 코루틴이 취소된 뒤 새로 뜬다. `snapshotFlow`는 읽기가 코루틴 안에 있어 composition 구독이 생기지 않고, 코루틴은 key가 바뀌지 않는 한 화면당 한 번만 뜬다.

`LaunchedEffect`의 key에는 그 Effect 자체가 다시 시작되어야 할 때만 바뀌는 것을 둔다. state 홀더와 ViewModel이 그렇다.

⚠️ 비권장 예시:

```kotlin
@Composable
private fun LoadWebPageEffect(
    pageViewModel: WebDetailPageViewModel,
    state: WebDetailScaffoldState,
) {
    // composition에서 읽으므로 표시 방식이 바뀔 때마다 다시 구성되고 코루틴이 다시 뜬다.
    val viewMode = state.viewMode

    LaunchedEffect(pageViewModel, viewMode) {
        if (viewMode == WebDetailViewMode.RESPONSE) {
            pageViewModel.load()
        }
    }
}
```

✅ 권장 예시:

```kotlin
@Composable
private fun LoadWebPageEffect(
    pageViewModel: WebDetailPageViewModel,
    state: WebDetailScaffoldState,
) {
    LaunchedEffect(state, pageViewModel) {
        snapshotFlow { state.viewMode }
            .filter { viewMode -> viewMode == WebDetailViewMode.RESPONSE }
            .collect { pageViewModel.load() }
    }
}
```

`snapshotFlow`는 값이 실제로 달라졌을 때만 방출하므로 `distinctUntilChanged`를 덧붙이지 않는다.

다음은 값을 key로 둔다.

- 읽을 state가 없고 진입과 이탈 자체가 계기인 경우. `LaunchedEffect(Unit)`과 `LifecycleEventEffect`가 그렇다.
- 값이 바뀌면 진행 중이던 작업을 취소하고 새 값으로 다시 시작해야 하는 경우. `snapshotFlow { }.collectLatest { }`로도 같은 결과를 내므로, 취소 경계가 코루틴 전체인 편이 읽기 쉬울 때만 key를 쓴다.

ViewModel이 노출하는 `Flow<XxxEffect>`를 UI가 받는 반대 방향은 `UI Effect 수집`을 따른다. 이 절은 UI가 소유한 state를 ViewModel이나 바깥으로 내보내는 방향만 다룬다.

## ViewModel 파라미터 이름

Screen 컴포저블이 받는 ViewModel 파라미터의 이름은 개수에 따라 정한다.

- **하나만 받으면 `viewModel`로 둔다.** 무엇을 가리키는지 모호할 여지가 없고, 화면 이름을 반복하면 타입 선언과 겹친다.
- **둘 이상 받으면 각각 그 ViewModel이 맡은 관심사를 이름으로 둔다**(`memoViewModel`, `syncViewModel`, `searchViewModel`, `placeMapViewModel`). 화면 이름은 접두사로 붙이지 않는다. 타입에 이미 들어 있고, 호출부에서 관심사만 구분되면 충분하다.

이 규칙은 내비게이션 진입점이 무엇을 주입하는지 드러내는 Screen 파라미터에 적용한다. Screen 안의 private Effect 컴포저블은 ViewModel을 하나만 받더라도 화면이 여러 ViewModel을 쓰면 어느 것을 넘기는지 호출부에서 드러나도록 관심사 이름을 그대로 쓴다.

✅ 권장 예시:

```kotlin
@Composable
internal fun TagDetailScreen(viewModel: TagDetailViewModel)

@Composable
internal fun MemoAddScreen(
    addViewModel: MemoAddViewModel,
    tagViewModel: MemoAddTagViewModel,
    placeViewModel: MemoAddPlaceViewModel,
    placeMapViewModel: MemoPlaceMapViewModel,
)

@Composable
private fun FetchCurrentLocationEffect(placeMapViewModel: MemoPlaceMapViewModel)
```

## UI Event 전달

Scaffold처럼 사용자 상호작용이 있는 화면 컴포저블은 상호작용마다 개별 콜백 파라미터를 나열하지 않고, sealed interface로 선언한 Event와 단일 `onEvent` 콜백으로 전달한다.

- Event 타입은 `XxxScaffoldEvent`처럼 컴포저블 이름을 접두사로 하는 sealed interface로 선언하고, 각 상호작용을 `data object` 또는 `data class`로 표현한다.
- Event에는 처리에 필요한 최소 데이터만 담는다. 모델 전체 대신 식별자(id)처럼 처리 측이 실제로 사용하는 값만 전달한다.
- Event의 해석(내비게이션, ViewModel 호출)은 Screen 컴포저블에서 `when` 분기로 처리한다.

✅ 권장 예시:

```kotlin
internal sealed interface MemoHomeScaffoldEvent {
    data object ClickAdd : MemoHomeScaffoldEvent

    data class ClickMemo(
        val id: Uuid,
    ) : MemoHomeScaffoldEvent
}

@Composable
internal fun MemoHomeScreen(
    navigateToAdd: () -> Unit,
    navigateToDetail: (Uuid) -> Unit,
) {
    MemoHomeScaffold(
        onEvent = { event ->
            when (event) {
                is MemoHomeScaffoldEvent.ClickAdd -> navigateToAdd()
                is MemoHomeScaffoldEvent.ClickMemo -> navigateToDetail(event.id)
            }
        },
    )
}
```

### 재사용 컴포저블의 Event

**`public`, `internal`로 재사용하는 컴포저블이 콜백을 3개 이상 노출하면 그 컴포저블이 소유하는 Event를 선언하고 `onEvent` 하나로 받는다.** 콜백을 나열하면 파라미터 목록과 호출부의 람다가 같이 늘고, 같은 컴포저블을 쓰는 화면마다 상위 Event에 같은 케이스를 다시 선언하게 된다.

- Event 이름은 그 컴포저블 묶음을 가리키는 이름으로 두고(`MemoListEvent`, `MemoTagPickerEvent`, `MemoFormEvent`), 같은 묶음의 하위 컴포저블(목록, 행, 버튼)은 새 Event를 만들지 않고 그 Event를 그대로 올린다. 하위 컴포저블의 콜백이 3개 미만이어도 같은 Event를 올려 번역 계층을 만들지 않는다.
- 다이얼로그의 표시 여부를 Host 컴포저블이 소유하면 `onDismissRequest`는 Event에 넣지 않고 파라미터로 남긴다. Host가 소비하는 상호작용을 Event에 넣으면 Host가 내보내지 않는 케이스가 상위 `when`에 남는다.
- 처리에 필요한 값을 그 컴포저블이 모르면 `xxxProvider: () -> T` 파라미터로 받아 Event를 만들 때 읽는다. 상위에서 Event를 다시 감싸 값을 채우지 않는다.

**Scaffold가 그런 컴포저블을 쓰면 자기 `XxxScaffoldEvent`와 섞지 않고 `onEvent`, `onXxxEvent`를 따로 받아 그대로 넘긴다.** Scaffold는 Event를 번역하지 않고, 해석은 Screen이 각 Event별 핸들러에서 한다. 여러 Scaffold가 같은 컴포저블을 쓰면 해석에 ViewModel이 필요 없는 핸들러는 그 컴포저블과 같은 패키지에 두고 함께 쓴다.

⚠️ 비권장 예시:

```kotlin
// Scaffold가 하위 컴포저블의 상호작용을 자기 Event로 번역한다.
MemoTagPickerDialogHost(
    dialogState = state.tagPickerDialogState,
    onTagSelect = { id -> onEvent(MemoAddScaffoldEvent.Tag.Select(id = id)) },
    onTagUnselect = { id -> onEvent(MemoAddScaffoldEvent.Tag.Unselect(id = id)) },
    onPrimaryTagSelect = { id -> onEvent(MemoAddScaffoldEvent.Tag.SelectPrimary(id = id)) },
    onPrimaryTagUnselect = { onEvent(MemoAddScaffoldEvent.Tag.UnselectPrimary) },
    onTagAdd = { onEvent(MemoAddScaffoldEvent.Tag.MoveToAdd) },
    onQueryChange = { query -> onEvent(MemoAddScaffoldEvent.Tag.ChangeQuery(query = query)) },
)
```

✅ 권장 예시:

```kotlin
@Composable
internal fun MemoAddScaffold(
    onEvent: (MemoAddScaffoldEvent) -> Unit,
    onFormEvent: (MemoFormEvent) -> Unit,
    onTagPickerEvent: (MemoTagPickerEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    MemoTagPickerDialogHost(
        dialogState = state.tagPickerDialogState,
        onEvent = onTagPickerEvent,
    )
}

@Composable
internal fun MemoAddScreen(navigateToTagAdd: () -> Unit) {
    MemoAddScaffold(
        onEvent = { event -> handleMemoAddEvent(event = event, ...) },
        onFormEvent = { event -> handleMemoFormEvent(event = event, ...) },
        onTagPickerEvent = { event -> handleMemoAddTagPickerEvent(event = event, ...) },
    )
}
```

## UI Effect 수집

ViewModel이 노출하는 one-shot Effect(`Flow<XxxEffect>`)를 UI에서 수집할 때는 다음을 따른다.

- Effect 수집 로직은 화면 컴포저블 본문에 두지 않고, 의도가 드러나는 이름의 별도 컴포저블 함수로 분리한다.
- Effect를 collect할 때는 `flowWithLifecycle`을 사용해 라이프사이클을 인지하며 수집한다.

✅ 권장 예시:

```kotlin
@Composable
private fun SignInEffect(
    effect: Flow<LoginHomeEffect>,
    navigateUp: () -> Unit,
) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    LaunchedEffect(effect, lifecycle) {
        effect
            .flowWithLifecycle(lifecycle)
            .collect { value ->
                when (value) {
                    is LoginHomeEffect.SignInSucceeded -> navigateUp()
                }
            }
    }
}
```

## Preview

**UI를 그리는 컴포저블에는 Preview를 하나 이상 둔다.** Preview는 앱을 실행하지 않고 표현을 확인하는 유일한 수단이고, 기본값만으로는 그려지지 않는 컴포저블을 컴파일 시점에 드러낸다.

- `public`, `internal` 컴포저블에는 Preview를 반드시 하나 이상 둔다.
- `private` 컴포저블은 필수가 아니지만, 표현이 상태에 따라 갈리면 두는 것이 좋다.
- Preview 함수는 대상과 같은 파일에 `private`으로 두고 이름을 `대상이름Preview`로 한다.
- 화면 단위는 `@ScreenPreview`, 그 이하 표현 단위는 `@ComponentPreview`를 붙이고 본문을 `DiaryTheme`으로 감싼다.
- 아이콘 하나만 그리는 컴포저블은 `compose:core`의 `@IconPreview`를 붙인다. 다크 모드에서 달라지는 표현이 없고 크기가 작아 밝기 두 벌을 나란히 둘 이유가 없으므로 배경만 켠 단일 Preview로 둔다. 본문은 다른 계층과 같이 `DiaryTheme`으로 감싼다.

### Preview를 두지 않는 컴포저블

다음은 UI를 그리지 않으므로 Preview를 두지 않는다.

- **Screen**: ViewModel과 내비게이션을 Scaffold에 연결하는 배선이고, 그리는 표현은 Scaffold의 Preview가 이미 보여준다. ViewModel을 필수 파라미터로 받아 Preview에서 만들 수도 없다.
- **Content**: Scaffold 슬롯 안에서 자기 ViewModel을 얻어 표현 컴포저블에 연결하는 배선이다. Screen과 같은 이유로 빼며, `ViewModelStoreProvider`와 Koin 문맥이 필요해 Preview에서 만들 수도 없다.
- **Effect**: 부수 효과만 수행하고 아무것도 그리지 않는다.
- **`rememberXxx` 팩토리**: state를 만들어 반환할 뿐 아무것도 그리지 않는다.

Content를 빼는 것은 다음 둘을 모두 만족할 때만이다. 하나라도 어긋나면 그 컴포저블은 표현을 갖고 있으므로 Preview를 둔다.

- 자기 ViewModel을 직접 얻는다. Screen이 주입해 주는 것을 받기만 하면 Content가 아니다.
- 자기 자신은 아무것도 그리지 않고 Preview가 있는 표현 컴포저블에 전부 위임한다. 레이아웃, 여백, 분기 표현이 Content 본문에 있으면 그 표현을 확인할 지점이 없어진다.

**대신 Scaffold에는 Preview를 반드시 둔다.** Screen이 빠지므로 화면 단위 표현을 확인할 수 있는 지점은 Scaffold뿐이다. Content가 위임하는 표현 컴포저블에도 같은 이유로 Preview를 둔다.

### 상태별 Preview는 @PreviewParameter로 공급

**표현이 상태에 따라 갈리면 상태마다 Preview 함수를 만들지 않고 `@PreviewParameter`로 값을 공급한다.** 같은 컴포저블의 Preview가 여럿이면 본문이 그대로 복제되어, 컴포저블의 파라미터가 바뀔 때 고쳐야 할 자리가 함께 늘어난다. `PreviewParameterProvider`는 달라지는 값만 모아 두므로 Preview 본문은 하나로 유지된다.

- Provider 이름은 값이 되는 타입에 `PreviewParameter`를 붙인다(`MemoDetailUiStatePreviewParameter`). 한 파일에서만 쓰면 그 파일에 `private`으로, 여러 파일이 공유하면 별도 파일에 `internal`로 둔다.
- `Boolean` 하나로 갈리는 표현은 `compose:core`의 `BooleanPreviewParameter`를 쓴다.
- 두 개 이상의 값이 함께 달라지면 Provider 타입을 `Pair`로 두지 않고 의미가 드러나는 `private data class`로 묶는다.
- 공급한 값에서 파생되는 나머지 인자는 Preview 본문에서 계산한다.

Provider의 `values`에 넣을 도메인 모델은 Preview마다 새로 만들지 않고 모듈의 `PreviewModel.kt`에 `previewXxx()` 팩토리로 두고 함께 쓴다.

⚠️ 비권장 예시:

```kotlin
@ScreenPreview
@Composable
private fun MemoAddScaffoldPreview() {
    DiaryTheme {
        MemoAddScaffold(onEvent = {})
    }
}

// 상태 하나가 늘 때마다 본문이 그대로 복제된다.
@ScreenPreview
@Composable
private fun MemoAddScaffoldInProgressPreview() {
    DiaryTheme {
        MemoAddScaffold(
            onEvent = {},
            uiStateProvider = { MemoAddUiState(isInProgress = true) },
        )
    }
}
```

✅ 권장 예시:

```kotlin
@ScreenPreview
@Composable
private fun MemoAddScaffoldPreview(
    @PreviewParameter(BooleanPreviewParameter::class) isInProgress: Boolean,
) {
    DiaryTheme {
        MemoAddScaffold(
            onEvent = {},
            uiStateProvider = { MemoAddUiState(isInProgress = isInProgress) },
        )
    }
}
```

✅ sealed UI state를 공급하는 예시:

```kotlin
private class MemoDetailUiStatePreviewParameter : PreviewParameterProvider<MemoDetailUiState> {
    override val values: Sequence<MemoDetailUiState> =
        sequenceOf(
            MemoDetailUiState.Loading,
            MemoDetailUiState.Content(
                id = Uuid.NIL,
                detail = MemoDetail.EMPTY.copy(title = "메모 제목"),
                isFinished = false,
            ),
        )
}

@ScreenPreview
@Composable
private fun MemoDetailScaffoldPreview(
    @PreviewParameter(MemoDetailUiStatePreviewParameter::class) uiState: MemoDetailUiState,
) {
    val detail = (uiState as? MemoDetailUiState.Content)?.detail ?: MemoDetail.EMPTY

    DiaryTheme {
        MemoDetailScaffold(
            onEvent = {},
            uiStateProvider = { uiState },
            state = rememberMemoDetailFormState(initialDetail = detail),
        )
    }
}
```

## 권한 관리자 공유

**한 Screen에서 `rememberPermissionManager()`는 한 번만 호출하고, 그 인스턴스를 권한 요청과 허용 여부 조회에 함께 넘긴다.** `rememberPermissionManager()`는 호출한 자리마다 새 인스턴스를 만들므로, 요청과 조회가 서로 다른 인스턴스를 쓰면 요청으로 바뀐 권한 상태가 조회 쪽 `isGranted`에 반영되지 않는다. 조회가 생명주기 복귀 때 다시 확인하면서 늦게 따라오는 것처럼 보이지만, 시스템 권한 요청이 화면 전환을 만들지 않는 플랫폼에서는 그마저 오지 않는다.

Android에서는 인스턴스마다 별도의 launcher가 등록되어 어느 쪽이 결과를 받는지도 갈린다.

Screen이 만든 인스턴스는 파라미터로 내려보낸다. 요청과 조회를 함께 쓰는 컴포저블은 `PermissionManager`를 파라미터로 받고, 기본값에만 `rememberPermissionManager()`를 둔다.

⚠️ 비권장 예시:

```kotlin
@Composable
internal fun PlaceHomeScreen() {
    // 조회가 자기 인스턴스를 만든다.
    val isGranted = rememberIsPermissionGranted(Permission.LOCATION)
    // 요청이 또 다른 인스턴스를 만든다. 허용을 받아도 위의 isGranted는 그대로다.
    val permissionManager = rememberPermissionManager()
    val scope = rememberCoroutineScope()

    DiaryMap(isMyLocationEnabled = isGranted)
    Button(onClick = { scope.launch { permissionManager.request(Permission.LOCATION) } })
}
```

✅ 권장 예시:

```kotlin
@Composable
internal fun PlaceHomeScreen(permissionManager: PermissionManager) {
    val isGranted = rememberIsPermissionGranted(Permission.LOCATION, permissionManager)
    val scope = rememberCoroutineScope()

    DiaryMap(isMyLocationEnabled = isGranted)
    Button(onClick = { scope.launch { permissionManager.request(Permission.LOCATION) } })
}
```

## 테스트를 위한 슬롯·파라미터 금지

운영 코드가 쓰지 않는 슬롯이나 파라미터를 테스트를 위해 컴포저블에 열지 않는다. 호출자가 하나뿐이고 항상 같은 내용을 넘기는 슬롯은 그 컴포저블의 구조가 아니라 테스트의 우회로이며, 읽는 사람에게 존재하지 않는 확장점을 있는 것처럼 보이게 한다.

슬롯은 운영에서 서로 다른 내용을 넣는 호출자가 둘 이상일 때만 연다. 예를 들어 지도를 슬롯으로 받는 레이아웃은 같은 지도를 두 배치가 나눠 쓰기 위한 것이므로 운영 구조다.

테스트하기 어려운 컴포저블은 다음 순서로 해결한다.

1. 검증할 규칙을 담은 부분을 의도가 드러나는 이름의 컴포저블이나 함수로 분리하고 그것을 직접 테스트한다. 기다림, 재실행 판정, 조건 분기처럼 표현이 아닌 규칙은 Effect 컴포저블로 분리하면 상태만 주고 검증할 수 있다.
2. 분리한 조각을 `internal`로 두어 같은 모듈의 테스트가 직접 구성하게 한다. 파라미터를 늘리지 않는다.
3. 그래도 결정적으로 자동화할 수 없으면 자동화하지 않는다. 테스트 케이스 문서에 `작성하지 않는 이유`를 남기고, 통과시키려고 운영 코드의 구조를 바꾸지 않는다.

⚠️ 비권장 예시:

```kotlin
@Composable
internal fun PlaceSearchDialog(
    state: PlaceSearchDialogState,
    // 운영 호출자는 하나뿐이고 항상 기본값을 쓴다. 테스트에서 지도를 빈 Box로 바꾸려고 연 슬롯이다.
    map: @Composable () -> Unit = { PlaceSearchMap(state = state) },
)
```

✅ 권장 예시:

```kotlin
@Composable
internal fun PlaceSearchDialog(state: PlaceSearchDialogState)

// 규칙은 표현과 분리해 두고 테스트가 직접 구성한다.
@Composable
internal fun PlaceSearchEffect(
    state: PlaceSearchDialogState,
    onSearch: (PlaceSearchRequest) -> Unit,
)
```

## Recomposition Loop (Cyclic Phase Dependency) 금지

레이아웃 결과(측정 크기, 위치)를 composition 단계에서 읽는 state에 기록해 다른 컴포저블의 크기나 배치를 결정하는 순환 단계 의존을 만들지 않는다. 이 패턴은 첫 프레임을 잘못된 값으로 그리고, 값이 바뀔 때마다 불필요한 recomposition 프레임을 추가한다.

- 판단 기준: https://developer.android.com/develop/ui/compose/phases#recomp-loop

컴포저블 간 크기나 위치가 서로 의존하면 custom `Layout`으로 한 번의 measure pass 안에서 해결한다. 형제의 측정 크기가 필요하면 부모 `Layout`에서 한 자식을 먼저 측정하고, 그 결과로 다른 자식의 constraints를 만든다. `IntrinsicSize`도 사용할 수 있으나 실측과 다를 수 있으므로(예: Material3 `TextField`의 intrinsic 높이) 크기 일치를 검증하는 테스트를 함께 둔다.

lazy 컨테이너처럼 custom `Layout`으로 표현할 수 없는 제약에서 부득이하게 state로 레이아웃 값을 전달할 때는, composition에서 읽지 않고 `Modifier.layout`이나 `Modifier.offset { }`처럼 layout 단계에서만 읽는다. 이는 완화책이며 기본 해법이 아니다.

⚠️ 비권장 예시:

```kotlin
@Composable
fun Example() {
    var height by remember { mutableIntStateOf(0) }

    Text(
        text = "Hello",
        modifier = Modifier.onSizeChanged { height = it.height },
    )
    Preview(
        modifier = Modifier.height(with(LocalDensity.current) { height.toDp() }),
    )
}
```

✅ 권장 예시:

```kotlin
@Composable
fun Example() {
    Layout(
        content = {
            Text(text = "Hello")
            Preview()
        },
    ) { measurables, constraints ->
        val text = measurables[0].measure(constraints)
        val preview = measurables[1].measure(
            constraints.copy(minHeight = text.height, maxHeight = text.height),
        )

        layout(constraints.maxWidth, text.height) {
            text.place(x = 0, y = 0)
            preview.place(x = text.width, y = 0)
        }
    }
}
```

## 자주 바뀌는 state의 파생 값

**자주 바뀌는 state를 읽어 드물게 바뀌는 값으로 줄인 결과를 composition에서 읽으면 `derivedStateOf`로 감싼다.** 여기서 `자주 바뀌는 state`는 사용자의 한 동작 동안 여러 번 바뀌는 값이다. `TextFieldState.text`는 글자마다, 스크롤·드래그 오프셋과 애니메이션 진행값은 프레임마다 바뀐다. 이런 값을 그대로 읽으면 읽은 컴포저블이 그 횟수만큼 recompose되지만, `derivedStateOf`로 감싸면 줄인 결과가 실제로 바뀔 때만 recompose된다.

- **상태 홀더가 그 값을 소유하면 홀더 안에서 `by derivedStateOf`로 선언한다.** 홀더는 `remember`로 만들어 컴포지션 동안 살아 있으므로 따로 `remember`로 감싸지 않는다. 읽는 컴포저블마다 같은 파생을 다시 선언하지 않아도 된다.
- **홀더 밖에서 만드는 지역 파생 값은 `remember(...) { derivedStateOf { ... } }`로 감싼다.** `remember` 없이 쓰면 recomposition마다 새 파생 state가 생겨 이득이 사라진다.

다음에는 쓰지 않는다. 감싸는 것 자체가 계산과 관찰 비용이므로, 줄어드는 recomposition이 없으면 손해만 남는다.

- 결과가 원본만큼 자주 바뀐다. 입력한 글자를 그대로 그리거나, 질의를 그대로 넘겨 다시 조회하는 경우가 그렇다.
- composition이 아니라 콜백, `LaunchedEffect`, `snapshotFlow` 안에서 읽는다. 이 읽기는 recomposition을 유발하지 않는다.
- 원본이 이미 드물게 바뀐다. `PagerState.currentPage`, 다이얼로그 표시 여부, 선택한 항목이 그렇다.

⚠️ 비권장 예시:

```kotlin
@Stable
internal class SearchHomeScaffoldState(
    val queryState: TextFieldState,
) {
    // 글자마다 바뀌는 값을 그대로 노출한다.
    val query: String
        get() = queryState.text.toString()
}

// 지우기 버튼은 있음과 없음만 오가는데, 글자를 칠 때마다 다시 그려진다.
trailingIcon = {
    if (state.queryState.text.isNotEmpty()) {
        ClearButton(onClick = state.queryState::clearText)
    }
}
```

✅ 권장 예시:

```kotlin
@Stable
internal class SearchHomeScaffoldState(
    val queryState: TextFieldState,
) {
    val hasQuery: Boolean by derivedStateOf { queryState.text.isNotEmpty() }
}

trailingIcon = {
    if (state.hasQuery) {
        ClearButton(onClick = state.queryState::clearText)
    }
}
```

## 시각 속성은 Style, 동작과 배치는 Modifier

**자체 컴포넌트의 한 노드에 시각 속성이 둘 이상 모이거나 시각 속성이 상태에 따라 달라지면 `Modifier.background`, `clip`, `padding`, `graphicsLayer`, `drawBehind`를 이어 붙이지 않고 `Modifier.styleable { }` 한 블록으로 선언한다.** Style은 속성 단위로 마지막 값이 이기는 한 겹의 선언이라 무엇이 어떻게 보이는지가 블록 하나에 모이고, 블록 안에서 읽은 state는 composition이 아니라 그 속성이 속한 단계만 다시 실행시킨다. 실험적 API이므로 `kotlin.md`의 `실험적 API Opt-in`을 따라 파일 상단에 `@file:OptIn(ExperimentalFoundationStyleApi::class)`를 둔다.

- 판단 기준: https://developer.android.com/develop/ui/compose/styles/styles-vs-modifiers
- 이 버전에서 쓸 수 있는 속성과 되지 않는 것은 [Compose Styles API 조사](../../docs/reference/compose-styles.md)가 소유한다.

Style로 옮기는 것은 모양, 배경, 테두리, 안쪽·바깥 여백, 크기, 투명도, 변형처럼 그 노드 자신의 보이는 속성이다. 다음은 Modifier로 남긴다.

| 남기는 것 | 이유 |
| --- | --- |
| `clickable`, `toggleable`, `semantics`, `testTag`, 제스처, 스크롤 | 동작과 의미는 Style이 표현하지 않는다 |
| `weight`, `align`, `animateItem`, `animatePlacement`, custom `Layout` | 부모 배치 규칙과 배치 전환은 Style 속성이 아니다 |
| 속성이 하나뿐인 노드의 `clip`, `padding`, `size` | Modifier 한 줄이 Style 블록보다 짧다 |
| Material 3 컴포넌트의 `colors`, `shape`, `border` 파라미터 | 이 버전의 Material 3는 `style` 파라미터를 받지 않는다 |
| `Modifier.shadow(elevation)` | Style의 `dropShadow`는 elevation 그림자와 모양이 다르다 |

**글자색과 글자 스타일은 Style로 내려보내지 않는다.** `contentColor`, `textStyle`의 상속은 이 버전에서 기본으로 꺼져 있고 켜도 Material 3 `Text`에 닿지 않는다. 지금처럼 `LocalContentColor`, `LocalTextStyle`, `Text`의 파라미터로 다룬다.

**`DiaryTheme.colorScheme`처럼 `@Composable` getter로만 읽는 값은 composition에서 지역 변수로 읽어 블록에 캡처한다.** Material 3의 색·모양·타이포그래피 CompositionLocal은 `internal`이라 `StyleScope`에서 `currentValue`로 읽을 수 없다. 반대로 state 홀더의 값은 블록 밖에서 읽어 넘기지 않고 블록 안에서 읽는다. 밖에서 읽으면 `state 읽기 지연`이 막는 composition 읽기가 다시 생긴다.

**상태에 따라 달라지는 시각 속성은 `MutableStyleState`와 상태 블록으로 선언한다.** 활성 여부는 `rememberUpdatedStyleState(interactionSource) { it.isEnabled = enabled }`로 넘기고 `disabled { }` 안에 비활성 표현을 둔다. 누름·호버·초점 표현이 필요한 자체 컴포넌트가 생기면 같은 `InteractionSource`를 `clickable`과 `rememberUpdatedStyleState`에 함께 넘긴다. 상태 사이의 전환은 디자인 문서가 정한 경우에만 `animate { }`로 감싼다.

**[공통 스타일](../../docs/design/styles.md)이 이름 붙인 묶음은 `compose:core`의 `DiaryStyles`에 같은 이름의 `Style` 값으로 두고, 호출부는 숫자를 다시 적지 않고 `DiaryTheme.styles`로 그 값을 쓴다.** 문서의 이름과 코드의 식별자가 일대일이어야 디자인이 바뀌었을 때 고칠 자리가 하나로 좁혀진다. 묶음이 [공통 여백과 간격](../../docs/design/dimens.md)의 값을 쓰면 `DiaryDimens`에 같은 이름의 값을 두고 `Style { }` 안에서 `LocalDiaryDimens.currentValue`로 읽는다. 여러 상태 블록 안에서 되풀이되는 묶음(`흐림`)은 `Style` 값 대신 `StyleScope` 확장 함수로 두고, `Shape`처럼 Style로 담을 수 없는 값은 그 값을 쓰는 컴포넌트 계열의 `XxxDefaults`에 둔다. 코드가 먼저 두 곳 이상에서 같은 시각 속성을 반복하게 되면 코드에 상수를 늘리지 않고 `design-wave`로 문서에 이름을 붙인 뒤 여기로 옮긴다.

**컴포넌트에 `style: Style` 파라미터는 다른 모양을 요구하는 두 번째 호출자가 생길 때 연다.** 그때 기본값은 `Style`로 두고, 컴포넌트 안에서 `Modifier.styleable(styleState, 기본 Style, style)`처럼 기본 Style 뒤에 붙여 호출자가 속성 단위로 덮어쓰게 한다. 호출자가 없는 파라미터를 미리 열지 않는 이유는 `테스트를 위한 슬롯·파라미터 금지`와 같다.

⚠️ 비권장 예시:

```kotlin
Text(
    text = text,
    modifier =
        modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color = color)
            .basicMarquee(iterations = Int.MAX_VALUE)
            .padding(1.dp),
)

DiaryFlexBox(
    modifier = modifier.graphicsLayer { alpha = if (isEnabled) 1F else DISABLED_ALPHA },
)
```

✅ 권장 예시:

```kotlin
@file:OptIn(ExperimentalFoundationStyleApi::class)

Text(
    text = text,
    modifier =
        modifier
            .styleable {
                shape(RoundedCornerShape(4.dp))
                clip()
                background(color)
                contentPadding(1.dp)
            }.basicMarquee(iterations = Int.MAX_VALUE),
)

val styleState = rememberUpdatedStyleState(interactionSource = null) { it.isEnabled = isEnabled }

DiaryFlexBox(
    modifier =
        modifier.styleable(styleState) {
            disabled { alpha(DISABLED_ALPHA) }
        },
)
```

✅ 공통 스타일 문서의 이름을 그대로 쓰는 예시:

```kotlin
// docs/design/styles.md의 `Bottom Sheet 제목`, `Bottom Sheet 내용`, `Bottom Sheet 선택 줄`
Text(
    text = title,
    modifier = Modifier.styleable(style = DiaryTheme.styles.bottomSheetTitle),
    style = DiaryTheme.typography.titleLargeEmphasized,
)

Column(
    modifier =
        Modifier
            .selectableGroup()
            .styleable(style = DiaryTheme.styles.bottomSheetContent),
)

Row(
    modifier =
        modifier
            .selectable(selected = isSelected, onClick = onClick, role = Role.RadioButton)
            .styleable(style = DiaryTheme.styles.bottomSheetRow),
)
```

✅ state 홀더의 값을 블록 안에서 읽는 예시:

```kotlin
val shape = MaterialTheme.shapes.medium

Column(
    modifier =
        modifier
            .styleable {
                shape(shape)
                clip()
                // 색이 바뀌면 다시 그리기만 하고 Column은 다시 구성되지 않는다.
                background(state.color)
            }.clickable(onClick = dialogState::show),
)
```
