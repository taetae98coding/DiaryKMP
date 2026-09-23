package io.github.taetae98coding.diary.feature.tag.ui.memo.finished

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
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
import io.github.taetae98coding.diary.compose.memo.list.MemoListState
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.feature.tag.ui.allDayMemoDateTime
import io.github.taetae98coding.diary.feature.tag.ui.tagMemo
import io.github.taetae98coding.diary.feature.tag.ui.tagMemoPagingData
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.ints.shouldBeLessThan
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Clock

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TagMemoFinishedListScaffoldTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-TAG-MEMO-FINISHED-LIST-FEATURE-003 상단 바에 대상 태그 표시와 완료된 메모 목록 표시를 함께 제공한다`() {
        setTagMemoFinishedListScaffold(uiState = TagMemoFinishedListUiState(title = EMOJI_WITH_TAG_TITLE))

        composeRule.onNodeWithText(EMOJI_WITH_TAG_TITLE).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_SUBTITLE).assertIsDisplayed()
    }

    @Test
    fun `TC-TAG-MEMO-FINISHED-LIST-FEATURE-003 이모지가 비어 있으면 상단 바에 제목만 표시한다`() {
        setTagMemoFinishedListScaffold(uiState = TagMemoFinishedListUiState(title = TAG_TITLE))

        composeRule.onNodeWithText(TAG_TITLE).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_SUBTITLE).assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-TAG-MEMO-FINISHED-LIST-FEATURE-003 한국어 환경에서 완료된 메모 목록 표시는 완료된 메모다`() {
        setTagMemoFinishedListScaffold(uiState = TagMemoFinishedListUiState(title = EMOJI_WITH_TAG_TITLE))

        composeRule.onNodeWithText(EMOJI_WITH_TAG_TITLE).assertIsDisplayed()
        composeRule.onNodeWithText(KOREAN_SUBTITLE).assertIsDisplayed()
    }

    @Test
    fun `TC-TAG-MEMO-FINISHED-LIST-FEATURE-004 태그 표시가 비어 있어도 완료된 메모 목록 표시와 뒤로가기는 유지한다`() {
        setTagMemoFinishedListScaffold()

        composeRule.onNodeWithText(EMOJI_WITH_TAG_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(TAG_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_SUBTITLE).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).assertIsDisplayed()
    }

    @Test
    fun `TC-TAG-MEMO-FINISHED-LIST-FEATURE-005 태그 표시가 바뀌면 바뀐 표시를 사용한다`() {
        val title = mutableStateOf(TAG_TITLE)
        setTagMemoFinishedListScaffold(uiStateProvider = { TagMemoFinishedListUiState(title = title.value) })

        composeRule.onNodeWithText(TAG_TITLE).assertIsDisplayed()

        composeRule.runOnIdle { title.value = EMOJI_WITH_UPDATED_TAG_TITLE }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(TAG_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(EMOJI_WITH_UPDATED_TAG_TITLE).assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경 뒤로가기 접근성 이름을 표시한다`() {
        setTagMemoFinishedListScaffold()

        composeRule.onNodeWithContentDescription(KOREAN_NAVIGATE_UP_DESCRIPTION).assertIsDisplayed()
    }

    @Test
    fun `TC-TAG-MEMO-FINISHED-LIST-FEATURE-012 TC-TAG-MEMO-FINISHED-LIST-DETAIL-FEATURE-016 이 화면에는 메모 추가 버튼을 표시하지 않는다`() {
        setTagMemoFinishedListScaffold()

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-TAG-MEMO-FINISHED-LIST-FEATURE-012 이 화면에는 완료된 메모 확인 버튼을 표시하지 않는다`() {
        setTagMemoFinishedListScaffold()

        composeRule.onNodeWithText(DEFAULT_FINISHED_LIST_LABEL).assertDoesNotExist()
    }

    @Test
    fun `TC-TAG-MEMO-FINISHED-LIST-FEATURE-002 TC-TAG-MEMO-FINISHED-LIST-FEATURE-006 메모 카드의 제목 컬러 기간과 날짜 헤더를 표시한다`() {
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
        setTagMemoFinishedListScaffold(
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
    fun `TC-TAG-MEMO-FINISHED-LIST-FEATURE-007 시작 날짜가 오늘인 메모의 날짜 그룹은 오늘임을 알린다`() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val memo = tagMemo(title = TODAY_MEMO_TITLE, dateTime = allDayMemoDateTime(today))
        setTagMemoFinishedListScaffold(
            pagingData =
                tagMemoPagingData(
                    itemList =
                        listOf(
                            MemoListItem.DateHeader(date = today),
                            MemoListItem.Content(memo = memo),
                        ),
                ),
            isTodayUpdated = true,
        )
        waitUntilMemoIsDisplayed(title = TODAY_MEMO_TITLE)

        composeRule.onNode(todayHeader(DEFAULT_TODAY_HEADER)).assertIsDisplayed()
    }

    @Test
    fun `TC-TAG-MEMO-FINISHED-LIST-FEATURE-009 최초 조회 실패는 메모 없이 오류와 재시도를 표시하지 않는다`() {
        val error = LoadState.Error(IllegalStateException("Refresh failed"))
        setTagMemoFinishedListScaffold(
            pagingData = tagMemoPagingData(itemList = emptyList(), refresh = error),
        )

        composeRule.onAllNodesWithTag(MEMO_COLOR_INDICATOR_TEST_TAG).assertCountEquals(0)
        composeRule.onNodeWithText(DEFAULT_ERROR_TEXT).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_RETRY_TEXT).assertDoesNotExist()
        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
    }

    @Test
    fun `TC-TAG-MEMO-FINISHED-LIST-FEATURE-009 추가 조회 실패는 표시된 메모를 유지하고 오류와 재시도를 표시하지 않는다`() {
        val memo = tagMemo(title = APPEND_ERROR_MEMO_TITLE)
        val error = LoadState.Error(IllegalStateException("Append failed"))
        setTagMemoFinishedListScaffold(
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
    fun `TC-TAG-MEMO-FINISHED-LIST-FEATURE-019 메모 카드를 선택하면 상세 이동을 요청한다`() {
        val memo = tagMemo(title = CLICK_MEMO_TITLE)
        val eventList = mutableListOf<MemoListEvent>()
        setTagMemoFinishedListScaffold(
            pagingData = tagMemoPagingData(itemList = listOf(MemoListItem.Content(memo = memo))),
            onMemoListEvent = eventList::add,
        )
        waitUntilMemoIsDisplayed(title = CLICK_MEMO_TITLE)

        composeRule.onNodeWithText(CLICK_MEMO_TITLE).performClick()

        eventList.shouldContainExactly(MemoListEvent.ClickMemo(id = memo.id))
    }

    @Test
    fun `TC-TAG-MEMO-FINISHED-LIST-FEATURE-016 왼쪽에서 오른쪽 스와이프는 다시 시작을 요청한다`() {
        val memo = tagMemo(title = RESTART_MEMO_TITLE)
        val eventList = mutableListOf<MemoListEvent>()
        setTagMemoFinishedListScaffold(
            pagingData = tagMemoPagingData(itemList = listOf(MemoListItem.Content(memo = memo))),
            onMemoListEvent = eventList::add,
        )
        waitUntilMemoIsDisplayed(title = RESTART_MEMO_TITLE)

        composeRule.onNodeWithText(RESTART_MEMO_TITLE).performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        eventList.shouldContainExactly(MemoListEvent.SwipeFinish(id = memo.id))
    }

    @Test
    fun `TC-TAG-MEMO-FINISHED-LIST-FEATURE-017 오른쪽에서 왼쪽 스와이프는 삭제를 요청한다`() {
        val memo = tagMemo(title = DELETE_MEMO_TITLE)
        val eventList = mutableListOf<MemoListEvent>()
        setTagMemoFinishedListScaffold(
            pagingData = tagMemoPagingData(itemList = listOf(MemoListItem.Content(memo = memo))),
            onMemoListEvent = eventList::add,
        )
        waitUntilMemoIsDisplayed(title = DELETE_MEMO_TITLE)

        composeRule.onNodeWithText(DELETE_MEMO_TITLE).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        eventList.shouldContainExactly(MemoListEvent.SwipeDelete(id = memo.id))
    }

    @Test
    fun `TC-TAG-MEMO-FINISHED-LIST-FEATURE-011 상단 바 뒤로가기는 화면 닫기를 요청한다`() {
        val eventList = mutableListOf<TagMemoFinishedListScaffoldEvent>()
        setTagMemoFinishedListScaffold(onEvent = eventList::add)

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()

        eventList.shouldContainExactly(TagMemoFinishedListScaffoldEvent.ClickNavigateUp)
    }

    private fun setTagMemoFinishedListScaffold(
        pagingData: PagingData<MemoListItem> = PagingData.empty(),
        uiState: TagMemoFinishedListUiState = TagMemoFinishedListUiState(),
        onEvent: (TagMemoFinishedListScaffoldEvent) -> Unit = {},
        onMemoListEvent: (MemoListEvent) -> Unit = {},
        uiStateProvider: () -> TagMemoFinishedListUiState = { uiState },
        isTodayUpdated: Boolean = false,
    ) {
        val pagingDataFlow = MutableStateFlow(pagingData)

        composeRule.setContent {
            DiaryTheme {
                TagMemoFinishedListScaffold(
                    memoListState = remember { MemoListState().apply { if (isTodayUpdated) updateToday() } },
                    snackbarHostState = remember { SnackbarHostState() },
                    memoPagingItems = pagingDataFlow.collectAsLazyPagingItems(),
                    onEvent = onEvent,
                    onMemoListEvent = onMemoListEvent,
                    uiStateProvider = uiStateProvider,
                )
            }
        }
    }

    private fun waitUntilMemoIsDisplayed(title: String) {
        composeRule.waitUntil(timeoutMillis = PAGING_ITEMS_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(title).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun todayHeader(text: String): SemanticsMatcher = hasTestTag(MEMO_DATE_HEADER_TEST_TAG) and hasText(text)

    private companion object {
        const val PAGING_ITEMS_TIMEOUT_MILLIS = 5_000L
        const val TAG_TITLE = "TagTitle"
        const val EMOJI_WITH_TAG_TITLE = "📌 TagTitle"
        const val EMOJI_WITH_UPDATED_TAG_TITLE = "📎 UpdatedTagTitle"
        const val DEFAULT_SUBTITLE = "Finished Memos"
        const val KOREAN_SUBTITLE = "완료된 메모"
        const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        const val KOREAN_NAVIGATE_UP_DESCRIPTION = "뒤로가기"
        const val DEFAULT_ADD_DESCRIPTION = "Add memo"
        const val DEFAULT_FINISHED_LIST_LABEL = "Finished memos"
        const val DEFAULT_ERROR_TEXT = "Error"
        const val DEFAULT_RETRY_TEXT = "Retry"
        const val DEFAULT_TODAY_HEADER = "Today"
        const val NO_DATE_TITLE = "NoDateMemo"
        const val ALL_DAY_TITLE = "AllDayMemo"
        const val DATE_TIME_TITLE = "DateTimeMemo"
        const val TODAY_MEMO_TITLE = "TodayMemo"
        const val APPEND_ERROR_MEMO_TITLE = "AppendErrorMemo"
        const val CLICK_MEMO_TITLE = "ClickMemo"
        const val RESTART_MEMO_TITLE = "RestartMemo"
        const val DELETE_MEMO_TITLE = "DeleteMemo"
        const val DISPLAYED_MEMO_COUNT = 3
    }
}
