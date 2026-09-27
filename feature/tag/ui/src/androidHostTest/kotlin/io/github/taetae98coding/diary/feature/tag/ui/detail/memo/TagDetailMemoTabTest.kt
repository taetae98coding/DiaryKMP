package io.github.taetae98coding.diary.feature.tag.ui.detail.memo

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.unit.height
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
import io.github.taetae98coding.diary.feature.tag.ui.allDayMemoDateTime
import io.github.taetae98coding.diary.feature.tag.ui.tagMemo
import io.github.taetae98coding.diary.feature.tag.ui.tagMemoPagingData
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.comparables.shouldBeLessThanOrEqualTo
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
class TagDetailMemoTabTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-TAG-DETAIL-MEMO-FEATURE-025 기본 환경 완료된 메모 확인 버튼을 표시한다`() {
        setTagDetailMemoTab()

        composeRule.onNodeWithText(DEFAULT_FINISHED_LIST_LABEL).assert(hasClickAction())
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-TAG-DETAIL-MEMO-FEATURE-025 한국어 환경 완료된 메모 확인 버튼을 표시한다`() {
        setTagDetailMemoTab()

        composeRule.onNodeWithText(KOREAN_FINISHED_LIST_LABEL).assert(hasClickAction())
    }

    @Test
    fun `TC-TAG-DETAIL-MEMO-FEATURE-025 완료된 메모 확인 버튼을 선택하면 완료된 메모 목록 이동을 요청한다`() {
        val eventList = mutableListOf<TagDetailMemoContentEvent>()
        setTagDetailMemoTab(onEvent = eventList::add)

        composeRule.onNodeWithText(DEFAULT_FINISHED_LIST_LABEL).performClick()

        eventList.shouldContainExactly(TagDetailMemoContentEvent.ClickFinishedList)
    }

    @Test
    @Config(qualifiers = "w320dp")
    fun `좁은 창에서 정렬 이름이 줄어들어도 완료된 메모 진입은 같은 줄에 그대로 표시한다`() {
        setTagDetailMemoTab(
            pagingData = tagMemoPagingData(itemList = listOf(MemoListItem.Content(memo = tagMemo(title = SORT_MEMO_TITLE)))),
            sort = ListSort.RECENTLY_UPDATED,
        )
        waitUntilMemoIsDisplayed(title = SORT_MEMO_TITLE)

        val sortBounds = composeRule.onNodeWithContentDescription(DEFAULT_SORT_DESCRIPTION).assertIsDisplayed().getBoundsInRoot()
        val finishedListBounds = composeRule.onNodeWithText(DEFAULT_FINISHED_LIST_LABEL).assertIsDisplayed().getBoundsInRoot()

        sortBounds.height shouldBe finishedListBounds.height
        sortBounds.right shouldBeLessThanOrEqualTo finishedListBounds.left
    }

    @Test
    fun `TC-TAG-DETAIL-MEMO-FEATURE-003 메모 카드의 제목 컬러 기간과 날짜 헤더를 표시한다`() {
        val date = LocalDate(year = 2026, month = 7, day = 19)
        val noDateMemo = tagMemo(title = NO_DATE_TITLE)
        val allDayMemo = tagMemo(title = ALL_DAY_TITLE, dateTime = allDayMemoDateTime(date))
        val dateTimeMemo =
            tagMemo(
                title = DATE_TIME_TITLE,
                dateTime =
                    MemoDateTime.DateTime(
                        start = LocalDateTime(year = 2026, month = 7, day = 19, hour = 9, minute = 0),
                        endInclusive = LocalDateTime(year = 2026, month = 7, day = 19, hour = 10, minute = 0),
                    ),
            )
        setTagDetailMemoTab(
            pagingData =
                tagMemoPagingData(
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
        composeRule
            .onAllNodesWithTag(
                testTag = MEMO_COLOR_INDICATOR_TEST_TAG,
                useUnmergedTree = true,
            ).assertCountEquals(DISPLAYED_MEMO_COUNT)
        composeRule
            .onAllNodesWithTag(
                testTag = MEMO_DATE_TIME_TEST_TAG,
                useUnmergedTree = true,
            ).assertCountEquals(2)
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
                .boundsInRoot
                .top
                .toInt()
        noDateTop.shouldBeLessThan(headerTop)
    }

    @Test
    fun `TC-TAG-DETAIL-MEMO-FEATURE-006 최초 조회 실패는 오류와 재시도 없이 빈 상태 안내를 표시한다`() {
        val error = LoadState.Error(IllegalStateException("Refresh failed"))
        setTagDetailMemoTab(
            pagingData = tagMemoPagingData(itemList = emptyList(), refresh = error),
        )

        composeRule.onAllNodesWithTag(MEMO_COLOR_INDICATOR_TEST_TAG).assertCountEquals(0)
        composeRule.onNodeWithText(DEFAULT_ERROR_TEXT).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_RETRY_TEXT).assertDoesNotExist()
        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithText(EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(EMPTY_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-TAG-DETAIL-MEMO-FEATURE-006 추가 조회 실패는 표시된 메모를 유지하고 오류와 재시도를 표시하지 않는다`() {
        val memo = tagMemo(title = APPEND_ERROR_MEMO_TITLE)
        val error = LoadState.Error(IllegalStateException("Append failed"))
        setTagDetailMemoTab(
            pagingData =
                tagMemoPagingData(
                    itemList = listOf(MemoListItem.Content(memo = memo)),
                    append = error,
                ),
        )

        composeRule.onNodeWithText(APPEND_ERROR_MEMO_TITLE).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_ERROR_TEXT).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_RETRY_TEXT).assertDoesNotExist()
    }

    @Test
    fun `TC-TAG-DETAIL-MEMO-FEATURE-007 메모 카드를 선택하면 상세 이동을 요청한다`() {
        val memo = tagMemo(title = CLICK_MEMO_TITLE)
        val eventList = mutableListOf<MemoListEvent>()
        setTagDetailMemoTab(
            pagingData =
                tagMemoPagingData(
                    itemList = listOf(MemoListItem.Content(memo = memo)),
                ),
            onMemoListEvent = eventList::add,
        )
        waitUntilMemoIsDisplayed(title = CLICK_MEMO_TITLE)

        composeRule.onNodeWithText(CLICK_MEMO_TITLE).performClick()

        eventList.shouldContainExactly(MemoListEvent.ClickMemo(id = memo.id))
    }

    @Test
    fun `TC-TAG-DETAIL-MEMO-FEATURE-008 왼쪽에서 오른쪽 스와이프는 완료를 요청한다`() {
        val memo = tagMemo(title = FINISH_MEMO_TITLE)
        val eventList = mutableListOf<MemoListEvent>()
        setTagDetailMemoTab(
            pagingData =
                tagMemoPagingData(
                    itemList = listOf(MemoListItem.Content(memo = memo)),
                ),
            onMemoListEvent = eventList::add,
        )
        waitUntilMemoIsDisplayed(title = FINISH_MEMO_TITLE)

        composeRule.onNodeWithText(FINISH_MEMO_TITLE).performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        eventList.shouldContainExactly(MemoListEvent.SwipeFinish(id = memo.id))
    }

    @Test
    fun `TC-TAG-DETAIL-MEMO-FEATURE-008 오른쪽에서 왼쪽 스와이프는 삭제를 요청한다`() {
        val memo = tagMemo(title = DELETE_MEMO_TITLE)
        val eventList = mutableListOf<MemoListEvent>()
        setTagDetailMemoTab(
            pagingData =
                tagMemoPagingData(
                    itemList = listOf(MemoListItem.Content(memo = memo)),
                ),
            onMemoListEvent = eventList::add,
        )
        waitUntilMemoIsDisplayed(title = DELETE_MEMO_TITLE)

        composeRule.onNodeWithText(DELETE_MEMO_TITLE).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        eventList.shouldContainExactly(MemoListEvent.SwipeDelete(id = memo.id))
    }

    private fun setTagDetailMemoTab(
        pagingData: PagingData<MemoListItem> = PagingData.empty(),
        onEvent: (TagDetailMemoContentEvent) -> Unit = {},
        onMemoListEvent: (MemoListEvent) -> Unit = {},
        sort: ListSort = ListSort.DEFAULT,
    ) {
        val pagingDataFlow = MutableStateFlow(pagingData)

        composeRule.setContent {
            DiaryTheme {
                TagDetailMemoTab(
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
        const val DEFAULT_FINISHED_LIST_LABEL = "Finished memos"
        const val EMPTY_TITLE = "No memos linked to this tag"
        const val EMPTY_DESCRIPTION = "Use the add button to create a memo."
        const val DEFAULT_SORT_DESCRIPTION = "List sort"
        const val KOREAN_FINISHED_LIST_LABEL = "완료된 메모"
        const val DEFAULT_ERROR_TEXT = "Error"
        const val DEFAULT_RETRY_TEXT = "Retry"
        const val NO_DATE_TITLE = "NoDateMemo"
        const val ALL_DAY_TITLE = "AllDayMemo"
        const val DATE_TIME_TITLE = "DateTimeMemo"
        const val APPEND_ERROR_MEMO_TITLE = "AppendErrorMemo"
        const val SORT_MEMO_TITLE = "SortMemo"
        const val CLICK_MEMO_TITLE = "ClickMemo"
        const val FINISH_MEMO_TITLE = "FinishMemo"
        const val DELETE_MEMO_TITLE = "DeleteMemo"
        const val DISPLAYED_MEMO_COUNT = 3
    }
}
