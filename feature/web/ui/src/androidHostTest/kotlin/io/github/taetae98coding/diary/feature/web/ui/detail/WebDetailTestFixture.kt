package io.github.taetae98coding.diary.feature.web.ui.detail

import androidx.activity.ComponentDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation3.runtime.result.LocalResultEventBus
import androidx.navigation3.runtime.result.ResultEventBus
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.memo.list.MemoListEffect
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.compose.memo.list.MemoListUiState
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.core.model.web.WebDetail
import io.github.taetae98coding.diary.core.model.web.WebHeader
import io.github.taetae98coding.diary.core.model.web.WebPage
import io.github.taetae98coding.diary.feature.web.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.web.ui.add.detailTagScreenTestViewModel
import io.github.taetae98coding.diary.feature.web.ui.detail.memo.WebDetailMemoSyncViewModel
import io.github.taetae98coding.diary.feature.web.ui.detail.memo.WebDetailMemoViewModel
import io.github.taetae98coding.diary.feature.web.ui.detail.page.WebDetailPageUiState
import io.github.taetae98coding.diary.feature.web.ui.detail.page.WebDetailPageViewModel
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.datetime.LocalDate
import org.koin.compose.KoinApplication
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module
import org.robolectric.shadows.ShadowDialog
import kotlin.time.Instant
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

internal const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
internal const val DEFAULT_OPEN_IN_NEW_DESCRIPTION = "Open externally"
internal const val DEFAULT_DELETE_DESCRIPTION = "Delete web"
internal const val DEFAULT_UPDATE_DESCRIPTION = "Update web"
internal const val DEFAULT_FORM_TAB_DESCRIPTION = "Edit web information"
internal const val DEFAULT_PAGE_TAB_DESCRIPTION = "View web page"
internal const val DEFAULT_MEMO_TAB_DESCRIPTION = "Memo"
internal const val KOREAN_MEMO_TAB_DESCRIPTION = "메모"
internal const val DEFAULT_MEMO_ADD_DESCRIPTION = "Add memo"
internal const val KOREAN_MEMO_ADD_DESCRIPTION = "메모 추가"
internal val FIRST_WEB_ID: Uuid = Uuid.parse("00000000-0000-0000-0000-000000000001")

private const val EFFECT_BUFFER_CAPACITY = 8

// KoinApplication에 넘긴 모듈은 첫 테스트의 것이 이어서 쓰이므로, 메모 탭이 얻는 값은 모듈이 붙잡는 이 흐름들로 테스트마다 바꾼다.
internal val memoPagingDataFlow = MutableStateFlow(PagingData.empty<MemoListItem>())
internal val memoListUiStateFlow = MutableStateFlow(MemoListUiState())
internal val memoEffectFlow = MutableSharedFlow<MemoListEffect>(extraBufferCapacity = EFFECT_BUFFER_CAPACITY)

internal var memoViewModelRef: WebDetailMemoViewModel? = null
    private set

internal var memoSyncViewModelRef: WebDetailMemoSyncViewModel? = null
    private set

private val webDetailTabViewModelModule =
    module {
        factory {
            mockk<WebDetailMemoViewModel>(relaxed = true)
                .apply {
                    every { memoPagingData } returns memoPagingDataFlow
                    every { sort } returns MutableStateFlow(ListSort.DEFAULT)
                    every { effect } returns memoEffectFlow
                }.also { memoViewModelRef = it }
        }
        factory {
            mockk<WebDetailMemoSyncViewModel>(relaxed = true)
                .apply { every { uiState } returns memoListUiStateFlow }
                .also { memoSyncViewModelRef = it }
        }
    }

internal fun prepareWebDetailTabViewModels(
    memoPagingData: PagingData<MemoListItem> = PagingData.empty(),
    memoListUiState: MemoListUiState = MemoListUiState(),
) {
    memoPagingDataFlow.value = memoPagingData
    memoListUiStateFlow.value = memoListUiState
    memoViewModelRef = null
    memoSyncViewModelRef = null
}

internal const val DEFAULT_HEADER_ADD_BUTTON_DESCRIPTION = "Add header"
internal const val DEFAULT_PAGE_DESCRIPTION = "Web page"
internal const val DEFAULT_RETRY_BUTTON = "Retry"
internal const val DEFAULT_VIEW_MODE_DESCRIPTION = "Web page view mode"
internal const val DEFAULT_VIEW_MODE_TITLE = "Web page view mode"
internal const val DEFAULT_URL_VIEW_MODE_LABEL = "URL mode"
internal const val DEFAULT_RESPONSE_VIEW_MODE_LABEL = "Response body mode"
internal const val DEFAULT_URL_VIEW_MODE_DESCRIPTION = "Saved request headers are not applied."

private const val TITLE_INPUT_INDEX = 0
private const val DESCRIPTION_INPUT_INDEX = 1
private const val URL_INPUT_INDEX = 2
private const val HEADER_NAME_INPUT_INDEX = 3
private const val HEADER_VALUE_INPUT_INDEX = 4
internal const val INPUT_COUNT_WITHOUT_HEADER = 3
internal const val INPUT_COUNT_PER_HEADER = 2

// FixtureMonkey는 빈 문자열도 생성하므로 표시 여부 검증에 쓰는 값은 비어 있지 않게 접두사를 붙인다.
internal fun testWebDetail(
    title: String = "제목-${fixtureMonkey.giveMeOne<String>()}",
    description: String = "설명-${fixtureMonkey.giveMeOne<String>()}",
    url: String = "https://example.com/${fixtureMonkey.giveMeOne<Int>()}",
    headerList: List<WebHeader> = emptyList(),
): WebDetail =
    WebDetail(
        title = title,
        description = description,
        url = url,
        headerList = headerList,
    )

internal fun testContentUiState(detail: WebDetail = testWebDetail()): WebDetailUiState.Content = WebDetailUiState.Content(id = Uuid.random(), detail = detail)

internal fun testWebPage(): WebPage = fixtureMonkey.giveMeOne<WebPage>()

internal fun ComposeContentTestRule.selectFormTab() {
    onNodeWithContentDescription(DEFAULT_FORM_TAB_DESCRIPTION).performClick()
    waitForIdle()
}

internal fun ComposeContentTestRule.openViewModeSheet() {
    onNodeWithContentDescription(DEFAULT_VIEW_MODE_DESCRIPTION).performClick()
    waitForIdle()
}

// 표시 방식 줄과 선택 목록이 같은 라벨을 그리므로 목록 안의 줄을 마지막 노드로 집는다.
internal fun ComposeContentTestRule.selectViewMode(label: String) {
    openViewModeSheet()
    onAllNodesWithText(label).onLast().performClick()
    waitForIdle()
}

// Bottom Sheet에는 닫기 버튼이 없으므로 아무 방식도 고르지 않고 닫는 조작인 뒤로가기를 다이얼로그 창에 전달한다.
internal fun ComposeContentTestRule.closeDialogByBack() {
    val dialog = ShadowDialog.getLatestDialog() as ComponentDialog

    runOnUiThread { dialog.onBackPressedDispatcher.onBackPressed() }
    waitForIdle()
}

internal fun ComposeContentTestRule.selectPageTab() {
    onNodeWithContentDescription(DEFAULT_PAGE_TAB_DESCRIPTION).performClick()
    waitForIdle()
}

internal fun ComposeContentTestRule.selectWebDetailTab(contentDescription: String) {
    onNodeWithContentDescription(contentDescription).performClick()
    waitForIdle()
}

internal fun memoScreenWebViewModel(
    uiState: StateFlow<WebDetailUiState> = MutableStateFlow(testContentUiState()),
    effect: Flow<WebDetailEffect> = emptyFlow(),
): WebDetailViewModel {
    val viewModel = mockk<WebDetailViewModel>()
    every { viewModel.uiState } returns uiState
    every { viewModel.effect } returns effect
    justRun { viewModel.update(any()) }
    justRun { viewModel.delete() }
    return viewModel
}

internal fun memoScreenPageViewModel(pageUiState: WebDetailPageUiState = WebDetailPageUiState.Loading): WebDetailPageViewModel {
    val viewModel = mockk<WebDetailPageViewModel>()
    every { viewModel.uiState } returns MutableStateFlow(pageUiState)
    justRun { viewModel.load() }
    justRun { viewModel.retry() }
    justRun { viewModel.refresh() }
    return viewModel
}

internal fun ComposeContentTestRule.setWebDetailMemoScreen(
    viewModel: WebDetailViewModel,
    id: Uuid = FIRST_WEB_ID,
    memoPagingData: PagingData<MemoListItem> = PagingData.empty(),
    memoListUiState: MemoListUiState = MemoListUiState(),
    navigateUp: () -> Unit = {},
    navigateToMemoAdd: () -> Unit = {},
    navigateToMemoDetail: (Uuid) -> Unit = {},
) {
    prepareWebDetailTabViewModels(memoPagingData = memoPagingData, memoListUiState = memoListUiState)

    setContent {
        WebDetailScreenTestTheme {
            WebDetailScreen(
                navigateUp = navigateUp,
                navigateToTagAdd = {},
                navigateToTagDetail = {},
                navigateToMemoAdd = navigateToMemoAdd,
                navigateToMemoDetail = navigateToMemoDetail,
                id = id,
                tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                webViewModel = viewModel,
                pageViewModel = memoScreenPageViewModel(),
                tagViewModel = detailTagScreenTestViewModel(),
            )
        }
    }
    waitForIdle()
}

internal fun webMemo(
    title: String,
    dateTime: MemoDateTime? = null,
): Memo =
    fixtureMonkey
        .giveMeKotlinBuilder<Memo>()
        .setExp(
            Memo::detail,
            fixtureMonkey.giveMeOne<MemoDetail>().copy(title = title, dateTime = dateTime),
        ).setExp(Memo::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
        .setExp(Memo::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
        .sample()

internal fun webMemoPagingData(
    itemList: List<MemoListItem>,
    refresh: LoadState = LoadState.NotLoading(endOfPaginationReached = true),
    append: LoadState = LoadState.NotLoading(endOfPaginationReached = true),
): PagingData<MemoListItem> =
    PagingData.from(
        data = itemList,
        sourceLoadStates =
            LoadStates(
                refresh = refresh,
                prepend = LoadState.NotLoading(endOfPaginationReached = true),
                append = append,
            ),
    )

internal fun allDayMemoDateTime(date: LocalDate): MemoDateTime.AllDay = MemoDateTime.AllDay(dateRange = date..date)

internal fun ComposeContentTestRule.titleInput(): SemanticsNodeInteraction = onAllNodes(hasSetTextAction())[TITLE_INPUT_INDEX]

internal fun ComposeContentTestRule.descriptionInput(): SemanticsNodeInteraction = onAllNodes(hasSetTextAction())[DESCRIPTION_INPUT_INDEX]

internal fun ComposeContentTestRule.urlInput(): SemanticsNodeInteraction = onAllNodes(hasSetTextAction())[URL_INPUT_INDEX]

internal fun ComposeContentTestRule.headerNameInput(row: Int = 0): SemanticsNodeInteraction = onAllNodes(hasSetTextAction())[HEADER_NAME_INPUT_INDEX + row * INPUT_COUNT_PER_HEADER]

internal fun ComposeContentTestRule.headerValueInput(row: Int = 0): SemanticsNodeInteraction = onAllNodes(hasSetTextAction())[HEADER_VALUE_INPUT_INDEX + row * INPUT_COUNT_PER_HEADER]

internal fun ComposeContentTestRule.inputCount(): Int = onAllNodes(hasSetTextAction()).fetchSemanticsNodes().size

internal fun ComposeContentTestRule.addHeaderRow() {
    onNodeWithContentDescription(DEFAULT_HEADER_ADD_BUTTON_DESCRIPTION).performClick()
    waitForIdle()
}

/**
 * WebDetail 화면은 결과 이벤트 버스를 [LocalResultEventBus]에서 읽고 메모 탭의 ViewModel을 Koin에서 얻으므로, 화면을 배치하는 테스트는 이 테마로 감싼다.
 */
@Composable
internal fun WebDetailScreenTestTheme(
    resultEventBus: ResultEventBus = ResultEventBus(),
    content: @Composable () -> Unit,
) {
    // 테스트 호스트 Activity의 ViewModelStore는 테스트 사이에 유지되므로,
    // 테스트마다 새 소유자를 제공해 이전 테스트의 탭별 ViewModel이 재사용되지 않게 한다.
    val viewModelStoreOwner =
        remember {
            object : ViewModelStoreOwner {
                override val viewModelStore: ViewModelStore = ViewModelStore()
            }
        }

    CompositionLocalProvider(
        LocalResultEventBus provides resultEventBus,
        LocalViewModelStoreOwner provides viewModelStoreOwner,
    ) {
        KoinApplication(configuration = koinConfiguration { modules(webDetailTabViewModelModule) }) {
            DiaryTheme(content = content)
        }
    }
}
