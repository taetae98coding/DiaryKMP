package io.github.taetae98coding.diary.feature.web.ui.detail.memo

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.WindowInfo
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.unit.IntSize
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.empty.DIARY_EMPTY_BOX_TEST_TAG
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.memo.MEMO_COLOR_INDICATOR_TEST_TAG
import io.github.taetae98coding.diary.compose.memo.MEMO_DATE_HEADER_TEST_TAG
import io.github.taetae98coding.diary.compose.memo.MEMO_DATE_TIME_TEST_TAG
import io.github.taetae98coding.diary.compose.memo.list.MemoListEvent
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.feature.web.ui.detail.WebDetailScaffoldContent
import io.github.taetae98coding.diary.feature.web.ui.detail.WebDetailScaffoldState
import io.github.taetae98coding.diary.feature.web.ui.detail.allDayMemoDateTime
import io.github.taetae98coding.diary.feature.web.ui.detail.tab.WebDetailTab
import io.github.taetae98coding.diary.feature.web.ui.detail.viewmode.WebDetailViewMode
import io.github.taetae98coding.diary.feature.web.ui.detail.webMemo
import io.github.taetae98coding.diary.feature.web.ui.detail.webMemoPagingData
import io.github.taetae98coding.diary.feature.web.ui.resetAndroidUiDispatcher
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.ints.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class WebDetailMemoTabTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-WEB-DETAIL-MEMO-FEATURE-018 메모 탭에는 완료된 메모 목록 진입 버튼을 두지 않는다`() {
        setMemoTab()

        composeRule.onNodeWithText(DEFAULT_FINISHED_LIST_LABEL).assertDoesNotExist()
        composeRule.onAllNodes(hasClickAction()).fetchSemanticsNodes().none { node -> node.config.toString().contains(DEFAULT_FINISHED_LIST_LABEL) } shouldBe true
    }

    @Test
    fun `TC-WEB-DETAIL-MEMO-FEATURE-024 정렬 컨트롤을 누르면 정렬 선택 요청을 전달한다`() {
        val eventList = mutableListOf<WebDetailMemoContentEvent>()
        setMemoTab(pagingData = webMemoPagingData(itemList = listOf(MemoListItem.Content(memo = webMemo(title = SORT_MEMO_TITLE)))), onEvent = eventList::add)
        waitUntilMemoIsDisplayed(title = SORT_MEMO_TITLE)

        composeRule.onNodeWithText(DEFAULT_SORT_LABEL).performClick()

        eventList.shouldContainExactly(WebDetailMemoContentEvent.ClickSort)
    }

    @Test
    fun `TC-WEB-DETAIL-MEMO-FEATURE-023 목록 위에 현재 정렬을 표시한다`() {
        setMemoTab(pagingData = webMemoPagingData(itemList = listOf(MemoListItem.Content(memo = webMemo(title = SORT_MEMO_TITLE)))))
        waitUntilMemoIsDisplayed(title = SORT_MEMO_TITLE)

        composeRule.onNodeWithText(DEFAULT_SORT_LABEL).assert(hasClickAction())
    }

    @Test
    fun `TC-WEB-DETAIL-MEMO-FEATURE-024 정렬 선택을 열면 세 정렬을 고를 수 있다`() {
        setMemoTab(
            pagingData = webMemoPagingData(itemList = listOf(MemoListItem.Content(memo = webMemo(title = SORT_MEMO_TITLE)))),
            sortSheetState = DialogState(isVisible = true),
        )

        composeRule.onNodeWithText(DEFAULT_SORT_SHEET_TITLE).assertExists()
        composeRule.onAllNodesWithText(DEFAULT_SORT_LABEL).onLast().assertExists()
        composeRule.onNodeWithText(DEFAULT_TITLE_SORT_LABEL).assertExists()
        composeRule.onNodeWithText(DEFAULT_RECENTLY_UPDATED_SORT_LABEL).assertExists()
    }

    @Test
    fun `TC-WEB-DETAIL-MEMO-FEATURE-025 제목순을 고르면 그 정렬을 전달하고 선택 목록이 닫힌다`() {
        assertSelectSort(label = DEFAULT_TITLE_SORT_LABEL, sort = ListSort.TITLE)
    }

    @Test
    fun `TC-WEB-DETAIL-MEMO-FEATURE-025 최근 수정순을 고르면 그 정렬을 전달하고 선택 목록이 닫힌다`() {
        assertSelectSort(label = DEFAULT_RECENTLY_UPDATED_SORT_LABEL, sort = ListSort.RECENTLY_UPDATED)
    }

    @Test
    fun `TC-WEB-DETAIL-MEMO-FEATURE-025 고른 정렬의 이름을 정렬 컨트롤에 표시한다`() {
        setMemoTab(
            pagingData = webMemoPagingData(itemList = listOf(MemoListItem.Content(memo = webMemo(title = SORT_MEMO_TITLE)))),
            sort = ListSort.RECENTLY_UPDATED,
        )
        waitUntilMemoIsDisplayed(title = SORT_MEMO_TITLE)

        composeRule.onNodeWithText(DEFAULT_RECENTLY_UPDATED_SORT_LABEL).assertExists()
        composeRule.onNodeWithText(DEFAULT_SORT_LABEL).assertDoesNotExist()
    }

    @Test
    fun `TC-WEB-DETAIL-MEMO-FEATURE-026 목록이 비어 있으면 정렬 컨트롤을 표시하지 않는다`() {
        setMemoTab(pagingData = webMemoPagingData(itemList = emptyList()))
        composeRule.waitUntil(timeoutMillis = PAGING_ITEMS_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithTag(DIARY_EMPTY_BOX_TEST_TAG).fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText(DEFAULT_SORT_LABEL).assertDoesNotExist()
    }

    @Test
    fun `TC-WEB-DETAIL-MEMO-FEATURE-001 기간 유무와 시작 날짜에 따라 메모를 나누어 표시한다`() {
        val date = LocalDate(year = 2026, month = 7, day = 19)
        val noDateMemo = webMemo(title = NO_DATE_TITLE)
        val allDayMemo = webMemo(title = ALL_DAY_TITLE, dateTime = allDayMemoDateTime(date))
        val dateTimeMemo =
            webMemo(
                title = DATE_TIME_TITLE,
                dateTime =
                    MemoDateTime.DateTime(
                        start = LocalDateTime(year = 2026, month = 7, day = 19, hour = 9, minute = 0),
                        endInclusive = LocalDateTime(year = 2026, month = 7, day = 19, hour = 10, minute = 0),
                    ),
            )
        setMemoTab(
            pagingData =
                webMemoPagingData(
                    itemList =
                        listOf(
                            MemoListItem.Content(memo = noDateMemo),
                            MemoListItem.DateHeader(date = date),
                            MemoListItem.Content(memo = allDayMemo),
                            MemoListItem.Content(memo = dateTimeMemo),
                        ),
                ),
        )
        waitUntilMemoIsDisplayed(title = NO_DATE_TITLE)

        composeRule.onNodeWithText(NO_DATE_TITLE).assertIsDisplayed()
        composeRule.onNodeWithText(ALL_DAY_TITLE).assertIsDisplayed()
        composeRule.onNodeWithText(DATE_TIME_TITLE).assertIsDisplayed()
        composeRule.onAllNodesWithTag(MEMO_COLOR_INDICATOR_TEST_TAG, useUnmergedTree = true).assertCountEquals(DISPLAYED_MEMO_COUNT)
        composeRule.onAllNodesWithTag(MEMO_DATE_TIME_TEST_TAG, useUnmergedTree = true).assertCountEquals(2)
        composeRule.onAllNodesWithTag(MEMO_DATE_HEADER_TEST_TAG).assertCountEquals(1)

        val noDateTop =
            composeRule
                .onNodeWithText(NO_DATE_TITLE)
                .fetchSemanticsNode()
                .boundsInRoot.top
                .toInt()
        val headerTop =
            composeRule
                .onAllNodesWithTag(MEMO_DATE_HEADER_TEST_TAG)[0]
                .fetchSemanticsNode()
                .boundsInRoot.top
                .toInt()
        noDateTop.shouldBeLessThan(headerTop)
    }

    @Test
    fun `TC-WEB-DETAIL-MEMO-FEATURE-003 최초 조회 실패는 오류와 재시도 없이 빈 상태 안내를 표시한다`() {
        val error = LoadState.Error(IllegalStateException("Refresh failed"))
        setMemoTab(pagingData = webMemoPagingData(itemList = emptyList(), refresh = error))

        composeRule.onAllNodesWithTag(MEMO_COLOR_INDICATOR_TEST_TAG).assertCountEquals(0)
        composeRule.onNodeWithText(DEFAULT_ERROR_TEXT).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_RETRY_TEXT).assertDoesNotExist()
        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-WEB-DETAIL-MEMO-FEATURE-003 추가 조회 실패는 표시된 메모를 유지하고 오류와 재시도를 표시하지 않는다`() {
        val memo = webMemo(title = APPEND_ERROR_MEMO_TITLE)
        val error = LoadState.Error(IllegalStateException("Append failed"))
        setMemoTab(pagingData = webMemoPagingData(itemList = listOf(MemoListItem.Content(memo = memo)), append = error))

        composeRule.onNodeWithText(APPEND_ERROR_MEMO_TITLE).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_ERROR_TEXT).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_RETRY_TEXT).assertDoesNotExist()
    }

    @Test
    fun `TC-WEB-DETAIL-MEMO-FEATURE-004 메모 카드를 선택하면 상세 이동을 요청한다`() {
        val memo = webMemo(title = CLICK_MEMO_TITLE)
        val eventList = mutableListOf<MemoListEvent>()
        setMemoTab(pagingData = webMemoPagingData(itemList = listOf(MemoListItem.Content(memo = memo))), onMemoListEvent = eventList::add)
        waitUntilMemoIsDisplayed(title = CLICK_MEMO_TITLE)

        composeRule.onNodeWithText(CLICK_MEMO_TITLE).performClick()

        eventList.shouldContainExactly(MemoListEvent.ClickMemo(id = memo.id))
    }

    @Test
    fun `TC-WEB-DETAIL-MEMO-FEATURE-005 왼쪽에서 오른쪽 스와이프는 완료를 요청한다`() {
        val memo = webMemo(title = FINISH_MEMO_TITLE)
        val eventList = mutableListOf<MemoListEvent>()
        setMemoTab(pagingData = webMemoPagingData(itemList = listOf(MemoListItem.Content(memo = memo))), onMemoListEvent = eventList::add)
        waitUntilMemoIsDisplayed(title = FINISH_MEMO_TITLE)

        composeRule.onNodeWithText(FINISH_MEMO_TITLE).performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        eventList.shouldContainExactly(MemoListEvent.SwipeFinish(id = memo.id))
    }

    @Test
    fun `TC-WEB-DETAIL-MEMO-FEATURE-005 오른쪽에서 왼쪽 스와이프는 삭제를 요청한다`() {
        val memo = webMemo(title = DELETE_MEMO_TITLE)
        val eventList = mutableListOf<MemoListEvent>()
        setMemoTab(pagingData = webMemoPagingData(itemList = listOf(MemoListItem.Content(memo = memo))), onMemoListEvent = eventList::add)
        waitUntilMemoIsDisplayed(title = DELETE_MEMO_TITLE)

        composeRule.onNodeWithText(DELETE_MEMO_TITLE).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        eventList.shouldContainExactly(MemoListEvent.SwipeDelete(id = memo.id))
    }

    @Test
    fun `TC-WEB-DETAIL-MEMO-DOMAIN-004 화면이 재생성되어도 메모 탭에서 보던 목록 위치가 유지된다`() {
        val titleList = positionMemoTitleList()
        val pagingDataFlow = MutableStateFlow(webMemoPagingData(itemList = titleList.map { title -> MemoListItem.Content(memo = webMemo(title = title)) }))
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            DiaryTheme {
                WebDetailMemoTab(
                    onEvent = {},
                    onMemoListEvent = {},
                    modifier = Modifier.fillMaxSize(),
                    memoPagingItems = pagingDataFlow.collectAsLazyPagingItems(),
                )
            }
        }
        waitUntilMemoIsDisplayed(title = titleList.first())
        composeRule.onNodeWithTag(WEB_DETAIL_MEMO_LIST_TEST_TAG).performScrollToNode(hasText(titleList.last()))
        composeRule.waitForIdle()
        composeRule.onNodeWithText(titleList.last()).assertIsDisplayed()

        restorationTester.emulateSavedInstanceStateRestore()
        waitUntilMemoIsDisplayed(title = titleList.last())

        composeRule.onNodeWithText(titleList.last()).assertIsDisplayed()
        composeRule.onNodeWithText(titleList.first()).assertIsNotDisplayed()
    }

    @Test
    fun `TC-WEB-DETAIL-MEMO-DOMAIN-004 창 너비가 바뀌어 배치가 달라져도 메모 탭에서 보던 목록 위치가 유지된다`() {
        val titleList = positionMemoTitleList()
        val pagingDataFlow = MutableStateFlow(webMemoPagingData(itemList = titleList.map { title -> MemoListItem.Content(memo = webMemo(title = title)) }))
        val containerSize = mutableStateOf(CompactWindowSize)
        val windowInfo = mockk<WindowInfo>()
        every { windowInfo.containerSize } answers { containerSize.value }
        every { windowInfo.isWindowFocused } returns true
        val scaffoldState = WebDetailScaffoldState(initialTab = WebDetailTab.MEMO, initialViewMode = WebDetailViewMode.URL)
        composeRule.setContent {
            CompositionLocalProvider(LocalWindowInfo provides windowInfo) {
                DiaryTheme {
                    WebDetailScaffoldContent(
                        onEvent = {},
                        onFormEvent = {},
                        modifier = Modifier.fillMaxSize(),
                        state = scaffoldState,
                        memoContent = {
                            WebDetailMemoTab(
                                onEvent = {},
                                onMemoListEvent = {},
                                modifier = Modifier.fillMaxSize(),
                                memoPagingItems = pagingDataFlow.collectAsLazyPagingItems(),
                            )
                        },
                    )
                }
            }
        }
        waitUntilMemoIsDisplayed(title = titleList.first())
        composeRule.onNodeWithTag(WEB_DETAIL_MEMO_LIST_TEST_TAG).performScrollToNode(hasText(titleList.last()))
        composeRule.waitForIdle()
        composeRule.onNodeWithText(titleList.last()).assertIsDisplayed()

        listOf(ExpandedWindowSize, CompactWindowSize).forEach { size ->
            composeRule.runOnIdle { containerSize.value = size }
            waitUntilMemoIsDisplayed(title = titleList.last())

            composeRule.onNodeWithText(titleList.last()).assertIsDisplayed()
            composeRule.onNodeWithText(titleList.first()).assertIsNotDisplayed()
        }
    }

    @Test
    fun `TC-WEB-DETAIL-MEMO-DOMAIN-005 앱을 다시 실행해 상세 화면에 새로 들어오면 메모 탭 목록을 맨 위부터 보여 준다`() {
        val titleList = positionMemoTitleList()
        val pagingDataFlow = MutableStateFlow(webMemoPagingData(itemList = titleList.map { title -> MemoListItem.Content(memo = webMemo(title = title)) }))
        var launchCount by mutableIntStateOf(0)
        composeRule.setContent {
            // 앱을 다시 실행하면 이전 실행의 화면 상태가 남지 않으므로, 저장 상태까지 새로 만드는 구성으로 재현한다.
            key(launchCount) {
                DiaryTheme {
                    WebDetailMemoTab(
                        onEvent = {},
                        onMemoListEvent = {},
                        modifier = Modifier.fillMaxSize(),
                        memoPagingItems = pagingDataFlow.collectAsLazyPagingItems(),
                    )
                }
            }
        }
        waitUntilMemoIsDisplayed(title = titleList.first())
        composeRule.onNodeWithTag(WEB_DETAIL_MEMO_LIST_TEST_TAG).performScrollToNode(hasText(titleList.last()))
        composeRule.waitForIdle()
        composeRule.onNodeWithText(titleList.last()).assertIsDisplayed()

        composeRule.runOnIdle { launchCount += 1 }
        waitUntilMemoIsDisplayed(title = titleList.first())

        composeRule.onNodeWithText(titleList.first()).assertIsDisplayed()
        composeRule.onNodeWithText(titleList.last()).assertIsNotDisplayed()
    }

    private fun positionMemoTitleList(): List<String> = List(POSITION_MEMO_COUNT) { index -> "$POSITION_MEMO_TITLE_PREFIX${index.toString().padStart(length = 2, padChar = '0')}" }

    private fun assertSelectSort(
        label: String,
        sort: ListSort,
    ) {
        val eventList = mutableListOf<WebDetailMemoContentEvent>()
        val sortSheetState = DialogState(isVisible = true)
        setMemoTab(
            pagingData = webMemoPagingData(itemList = listOf(MemoListItem.Content(memo = webMemo(title = SORT_MEMO_TITLE)))),
            onEvent = eventList::add,
            sortSheetState = sortSheetState,
        )

        composeRule.onNodeWithText(label).performClick()
        composeRule.waitForIdle()

        eventList.shouldContainExactly(WebDetailMemoContentEvent.SelectSort(sort = sort))
        sortSheetState.isVisible shouldBe false
    }

    private fun setMemoTab(
        pagingData: PagingData<MemoListItem> = PagingData.empty(),
        onEvent: (WebDetailMemoContentEvent) -> Unit = {},
        onMemoListEvent: (MemoListEvent) -> Unit = {},
        sort: ListSort = ListSort.DEFAULT,
        sortSheetState: DialogState = DialogState(),
    ) {
        val pagingDataFlow = MutableStateFlow(pagingData)

        composeRule.setContent {
            DiaryTheme {
                WebDetailMemoTab(
                    onEvent = onEvent,
                    onMemoListEvent = onMemoListEvent,
                    modifier = Modifier.fillMaxSize(),
                    sortSheetState = sortSheetState,
                    memoPagingItems = pagingDataFlow.collectAsLazyPagingItems(),
                    sortProvider = { sort },
                )
            }
        }
    }

    private fun waitUntilMemoIsDisplayed(title: String) {
        composeRule.waitUntil(timeoutMillis = PAGING_ITEMS_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(title).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private companion object {
        const val PAGING_ITEMS_TIMEOUT_MILLIS = 5_000L
        const val DISPLAYED_MEMO_COUNT = 3
        const val NO_DATE_TITLE = "WebMemoNoDate"
        const val ALL_DAY_TITLE = "WebMemoAllDay"
        const val DATE_TIME_TITLE = "WebMemoDateTime"
        const val APPEND_ERROR_MEMO_TITLE = "WebMemoAppendError"
        const val SORT_MEMO_TITLE = "WebMemoSort"
        const val CLICK_MEMO_TITLE = "WebMemoClick"
        const val FINISH_MEMO_TITLE = "WebMemoFinish"
        const val DELETE_MEMO_TITLE = "WebMemoDelete"
        const val DEFAULT_ERROR_TEXT = "Error"
        const val DEFAULT_EMPTY_TITLE = "No memos linked to this web"
        const val DEFAULT_EMPTY_DESCRIPTION = "Use the add button to create a memo."
        const val DEFAULT_RETRY_TEXT = "Retry"
        const val DEFAULT_FINISHED_LIST_LABEL = "Finished memos"
        const val DEFAULT_SORT_LABEL = "Default"
        const val POSITION_MEMO_COUNT = 30
        val CompactWindowSize = IntSize(width = 400, height = 800)
        val ExpandedWindowSize = IntSize(width = 2_000, height = 800)
        const val POSITION_MEMO_TITLE_PREFIX = "WebMemoPosition"
        const val DEFAULT_SORT_SHEET_TITLE = "Sort"
        const val DEFAULT_TITLE_SORT_LABEL = "Title"
        const val DEFAULT_RECENTLY_UPDATED_SORT_LABEL = "Recently updated"
    }
}
