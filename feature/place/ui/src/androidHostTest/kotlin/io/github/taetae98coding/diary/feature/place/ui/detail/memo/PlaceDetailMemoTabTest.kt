package io.github.taetae98coding.diary.feature.place.ui.detail.memo

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.empty.DIARY_EMPTY_BOX_TEST_TAG
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.memo.MEMO_COLOR_INDICATOR_TEST_TAG
import io.github.taetae98coding.diary.compose.memo.MEMO_DATE_HEADER_TEST_TAG
import io.github.taetae98coding.diary.compose.memo.MEMO_DATE_TIME_TEST_TAG
import io.github.taetae98coding.diary.compose.memo.list.MemoListEvent
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.feature.place.ui.detail.allDayMemoDateTime
import io.github.taetae98coding.diary.feature.place.ui.detail.placeMemo
import io.github.taetae98coding.diary.feature.place.ui.detail.placeMemoPagingData
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.ints.shouldBeLessThan
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlaceDetailMemoTabTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-PLACE-DETAIL-MEMO-FEATURE-018 메모 탭에는 완료된 메모 목록 진입 버튼을 두지 않는다`() {
        setMemoTab()

        composeRule.onNodeWithText(DEFAULT_FINISHED_LIST_LABEL).assertDoesNotExist()
        composeRule.onAllNodes(hasClickAction()).fetchSemanticsNodes().none { node -> node.config.toString().contains(DEFAULT_FINISHED_LIST_LABEL) } shouldBe true
    }

    @Test
    fun `정렬 컨트롤을 누르면 정렬 선택 요청을 전달한다`() {
        val eventList = mutableListOf<PlaceDetailMemoContentEvent>()
        setMemoTab(onEvent = eventList::add)

        composeRule.onNodeWithText(DEFAULT_SORT_LABEL).performClick()

        eventList.shouldContainExactly(PlaceDetailMemoContentEvent.ClickSort)
    }

    @Test
    fun `TC-PLACE-DETAIL-MEMO-FEATURE-001 기간 유무와 시작 날짜에 따라 메모를 나누어 표시한다`() {
        val date = LocalDate(year = 2026, month = 7, day = 19)
        val noDateMemo = placeMemo(title = NO_DATE_TITLE)
        val allDayMemo = placeMemo(title = ALL_DAY_TITLE, dateTime = allDayMemoDateTime(date))
        val dateTimeMemo =
            placeMemo(
                title = DATE_TIME_TITLE,
                dateTime =
                    MemoDateTime.DateTime(
                        start = LocalDateTime(year = 2026, month = 7, day = 19, hour = 9, minute = 0),
                        endInclusive = LocalDateTime(year = 2026, month = 7, day = 19, hour = 10, minute = 0),
                    ),
            )
        setMemoTab(
            pagingData =
                placeMemoPagingData(
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
    fun `TC-PLACE-DETAIL-MEMO-FEATURE-003 최초 조회 실패는 메모 없이 오류와 재시도를 표시하지 않는다`() {
        val error = LoadState.Error(IllegalStateException("Refresh failed"))
        setMemoTab(pagingData = placeMemoPagingData(itemList = emptyList(), refresh = error))

        composeRule.onAllNodesWithTag(MEMO_COLOR_INDICATOR_TEST_TAG).assertCountEquals(0)
        composeRule.onNodeWithText(DEFAULT_ERROR_TEXT).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_RETRY_TEXT).assertDoesNotExist()
        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
    }

    @Test
    fun `TC-PLACE-DETAIL-MEMO-FEATURE-003 추가 조회 실패는 표시된 메모를 유지하고 오류와 재시도를 표시하지 않는다`() {
        val memo = placeMemo(title = APPEND_ERROR_MEMO_TITLE)
        val error = LoadState.Error(IllegalStateException("Append failed"))
        setMemoTab(pagingData = placeMemoPagingData(itemList = listOf(MemoListItem.Content(memo = memo)), append = error))

        composeRule.onNodeWithText(APPEND_ERROR_MEMO_TITLE).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_ERROR_TEXT).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_RETRY_TEXT).assertDoesNotExist()
    }

    @Test
    fun `TC-PLACE-DETAIL-MEMO-FEATURE-004 메모 카드를 선택하면 상세 이동을 요청한다`() {
        val memo = placeMemo(title = CLICK_MEMO_TITLE)
        val eventList = mutableListOf<MemoListEvent>()
        setMemoTab(pagingData = placeMemoPagingData(itemList = listOf(MemoListItem.Content(memo = memo))), onMemoListEvent = eventList::add)
        waitUntilMemoIsDisplayed(title = CLICK_MEMO_TITLE)

        composeRule.onNodeWithText(CLICK_MEMO_TITLE).performClick()

        eventList.shouldContainExactly(MemoListEvent.ClickMemo(id = memo.id))
    }

    @Test
    fun `TC-PLACE-DETAIL-MEMO-FEATURE-005 왼쪽에서 오른쪽 스와이프는 완료를 요청한다`() {
        val memo = placeMemo(title = FINISH_MEMO_TITLE)
        val eventList = mutableListOf<MemoListEvent>()
        setMemoTab(pagingData = placeMemoPagingData(itemList = listOf(MemoListItem.Content(memo = memo))), onMemoListEvent = eventList::add)
        waitUntilMemoIsDisplayed(title = FINISH_MEMO_TITLE)

        composeRule.onNodeWithText(FINISH_MEMO_TITLE).performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        eventList.shouldContainExactly(MemoListEvent.SwipeFinish(id = memo.id))
    }

    @Test
    fun `TC-PLACE-DETAIL-MEMO-FEATURE-005 오른쪽에서 왼쪽 스와이프는 삭제를 요청한다`() {
        val memo = placeMemo(title = DELETE_MEMO_TITLE)
        val eventList = mutableListOf<MemoListEvent>()
        setMemoTab(pagingData = placeMemoPagingData(itemList = listOf(MemoListItem.Content(memo = memo))), onMemoListEvent = eventList::add)
        waitUntilMemoIsDisplayed(title = DELETE_MEMO_TITLE)

        composeRule.onNodeWithText(DELETE_MEMO_TITLE).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        eventList.shouldContainExactly(MemoListEvent.SwipeDelete(id = memo.id))
    }

    private fun setMemoTab(
        pagingData: PagingData<MemoListItem> = PagingData.empty(),
        onEvent: (PlaceDetailMemoContentEvent) -> Unit = {},
        onMemoListEvent: (MemoListEvent) -> Unit = {},
        sort: ListSort = ListSort.DEFAULT,
    ) {
        val pagingDataFlow = MutableStateFlow(pagingData)

        composeRule.setContent {
            DiaryTheme {
                PlaceDetailMemoTab(
                    onEvent = onEvent,
                    onMemoListEvent = onMemoListEvent,
                    modifier = Modifier.fillMaxSize(),
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
        const val NO_DATE_TITLE = "PlaceMemoNoDate"
        const val ALL_DAY_TITLE = "PlaceMemoAllDay"
        const val DATE_TIME_TITLE = "PlaceMemoDateTime"
        const val APPEND_ERROR_MEMO_TITLE = "PlaceMemoAppendError"
        const val CLICK_MEMO_TITLE = "PlaceMemoClick"
        const val FINISH_MEMO_TITLE = "PlaceMemoFinish"
        const val DELETE_MEMO_TITLE = "PlaceMemoDelete"
        const val DEFAULT_ERROR_TEXT = "Error"
        const val DEFAULT_RETRY_TEXT = "Retry"
        const val DEFAULT_FINISHED_LIST_LABEL = "Finished memos"
        const val DEFAULT_SORT_LABEL = "Default"
    }
}
