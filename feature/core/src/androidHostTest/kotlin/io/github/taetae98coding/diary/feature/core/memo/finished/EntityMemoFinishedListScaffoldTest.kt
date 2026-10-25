package io.github.taetae98coding.diary.feature.core.memo.finished

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.semantics.SemanticsActions
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
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.text.TextLayoutResult
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
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.ints.shouldBeLessThan
import io.kotest.matchers.shouldBe
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
class EntityMemoFinishedListScaffoldTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-0fe6f585-5b4f-4239-b7fe-1107d3fc26ef 상단 바에 대상 항목의 이름과 완료된 메모 목록 표시를 함께 제공한다`() {
        setEntityMemoFinishedListScaffold(uiState = EntityMemoFinishedListUiState(title = ENTITY_NAME))

        composeRule.onNodeWithText(ENTITY_NAME).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_SUBTITLE).assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-0fe6f585-5b4f-4239-b7fe-1107d3fc26ef 한국어 환경에서 완료된 메모 목록 표시는 완료된 메모다`() {
        setEntityMemoFinishedListScaffold(uiState = EntityMemoFinishedListUiState(title = ENTITY_NAME))

        composeRule.onNodeWithText(ENTITY_NAME).assertIsDisplayed()
        composeRule.onNodeWithText(KOREAN_SUBTITLE).assertIsDisplayed()
    }

    @Test
    fun `TC-3a803196-7a36-45a1-a26a-a896965402f3 항목 이름이 비어 있어도 완료된 메모 목록 표시와 뒤로가기는 유지한다`() {
        setEntityMemoFinishedListScaffold()

        composeRule.onNodeWithText(ENTITY_NAME).assertDoesNotExist()
        composeRule.onNodeWithText(OTHER_ENTITY_NAME).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_SUBTITLE).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).assertIsDisplayed()
    }

    @Test
    fun `TC-7cb4d67b-a6e8-474a-a12c-457476e3212b 항목 이름이 바뀌면 바뀐 이름을 사용한다`() {
        val title = mutableStateOf(OTHER_ENTITY_NAME)
        setEntityMemoFinishedListScaffold(uiStateProvider = { EntityMemoFinishedListUiState(title = title.value) })

        composeRule.onNodeWithText(OTHER_ENTITY_NAME).assertIsDisplayed()

        composeRule.runOnIdle { title.value = UPDATED_ENTITY_NAME }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(OTHER_ENTITY_NAME).assertDoesNotExist()
        composeRule.onNodeWithText(UPDATED_ENTITY_NAME).assertIsDisplayed()
    }

    @Test
    fun `TC-1d593189-0522-450d-80d4-b96db237e33b 상단 바의 항목 이름은 폭을 넘어도 한 줄에 생략 없이 담긴다`() {
        setEntityMemoFinishedListScaffold(uiState = EntityMemoFinishedListUiState(title = LONG_ENTITY_NAME))

        val textLayoutResults = mutableListOf<TextLayoutResult>()
        composeRule
            .onNodeWithText(text = LONG_ENTITY_NAME, useUnmergedTree = true)
            .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { action -> action(textLayoutResults) }
        val textLayoutResult = textLayoutResults.single()

        textLayoutResult.lineCount shouldBe 1
        textLayoutResult.isLineEllipsized(lineIndex = 0) shouldBe false
        textLayoutResult.getLineEnd(lineIndex = 0, visibleEnd = false) shouldBe LONG_ENTITY_NAME.length
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경 뒤로가기 접근성 이름을 표시한다`() {
        setEntityMemoFinishedListScaffold()

        composeRule.onNodeWithContentDescription(KOREAN_NAVIGATE_UP_DESCRIPTION).assertIsDisplayed()
    }

    @Test
    fun `TC-1b064e48-363f-48a1-b87e-628749605c2e TC-81d2c48e-04c1-484d-b56d-44faad51a460 이 화면에는 메모 추가 버튼을 표시하지 않는다`() {
        setEntityMemoFinishedListScaffold()

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `이 화면에는 완료된 메모 확인 버튼을 표시하지 않는다`() {
        setEntityMemoFinishedListScaffold()

        composeRule.onNodeWithText(DEFAULT_FINISHED_LIST_LABEL).assertDoesNotExist()
    }

    @Test
    fun `TC-e338764a-df6b-4894-97ca-e8ab6a1baeaa TC-c247eb4b-2ac6-4da0-9439-0b7e57b8354e 메모 카드의 제목 컬러 기간과 날짜 헤더를 표시한다`() {
        val date = LocalDate(year = 2026, month = 7, day = 19)
        val noDateMemo = entityMemo(title = NO_DATE_TITLE)
        val allDayMemo = entityMemo(title = ALL_DAY_TITLE, dateTime = MemoDateTime.AllDay(dateRange = date..date))
        val dateTimeMemo =
            entityMemo(
                title = DATE_TIME_TITLE,
                dateTime =
                    MemoDateTime.DateTime(
                        start = LocalDateTime(year = 2026, month = 7, day = 19, hour = 9, minute = 0),
                        endInclusive = LocalDateTime(year = 2026, month = 7, day = 19, hour = 10, minute = 0),
                    ),
            )
        setEntityMemoFinishedListScaffold(
            pagingData =
                entityMemoPagingData(
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
    fun `TC-e975d9f9-548c-412f-95a9-3c161fc048ee 시작 날짜가 오늘인 메모의 날짜 그룹은 오늘임을 알린다`() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val memo = entityMemo(title = TODAY_MEMO_TITLE, dateTime = MemoDateTime.AllDay(dateRange = today..today))
        setEntityMemoFinishedListScaffold(
            pagingData =
                entityMemoPagingData(
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
    fun `TC-a0008899-ace5-47f4-9c4c-279a5e617f7c 최초 조회 실패는 메모 없이 오류와 재시도를 표시하지 않는다`() {
        val error = LoadState.Error(IllegalStateException("Refresh failed"))
        setEntityMemoFinishedListScaffold(
            pagingData = entityMemoPagingData(itemList = emptyList(), refresh = error),
        )

        composeRule.onAllNodesWithTag(MEMO_COLOR_INDICATOR_TEST_TAG).assertCountEquals(0)
        composeRule.onNodeWithText(DEFAULT_ERROR_TEXT).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_RETRY_TEXT).assertDoesNotExist()
        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
    }

    @Test
    fun `TC-a0008899-ace5-47f4-9c4c-279a5e617f7c 추가 조회 실패는 표시된 메모를 유지하고 오류와 재시도를 표시하지 않는다`() {
        val memo = entityMemo(title = APPEND_ERROR_MEMO_TITLE)
        val error = LoadState.Error(IllegalStateException("Append failed"))
        setEntityMemoFinishedListScaffold(
            pagingData =
                entityMemoPagingData(
                    itemList = listOf(MemoListItem.Content(memo = memo)),
                    append = error,
                ),
        )

        composeRule.onNodeWithText(APPEND_ERROR_MEMO_TITLE).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_ERROR_TEXT).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_RETRY_TEXT).assertDoesNotExist()
    }

    @Test
    fun `TC-6176bb69-c13d-4d94-8d82-ec488cf53c3c 메모 카드를 선택하면 상세 이동을 요청한다`() {
        val memo = entityMemo(title = CLICK_MEMO_TITLE)
        val eventList = mutableListOf<MemoListEvent>()
        setEntityMemoFinishedListScaffold(
            pagingData = entityMemoPagingData(itemList = listOf(MemoListItem.Content(memo = memo))),
            onMemoListEvent = eventList::add,
        )
        waitUntilMemoIsDisplayed(title = CLICK_MEMO_TITLE)

        composeRule.onNodeWithText(CLICK_MEMO_TITLE).performClick()

        eventList.shouldContainExactly(MemoListEvent.ClickMemo(id = memo.id))
    }

    @Test
    fun `TC-3162b5e5-0c0b-4397-80b5-34b37ec951d6 왼쪽에서 오른쪽 스와이프는 다시 시작을 요청한다`() {
        val memo = entityMemo(title = RESTART_MEMO_TITLE)
        val eventList = mutableListOf<MemoListEvent>()
        setEntityMemoFinishedListScaffold(
            pagingData = entityMemoPagingData(itemList = listOf(MemoListItem.Content(memo = memo))),
            onMemoListEvent = eventList::add,
        )
        waitUntilMemoIsDisplayed(title = RESTART_MEMO_TITLE)

        composeRule.onNodeWithText(RESTART_MEMO_TITLE).performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        eventList.shouldContainExactly(MemoListEvent.SwipeRestart(id = memo.id))
    }

    @Test
    fun `TC-de3ead05-f69f-4c5e-9001-934c11e284b1 오른쪽에서 왼쪽 스와이프는 삭제를 요청한다`() {
        val memo = entityMemo(title = DELETE_MEMO_TITLE)
        val eventList = mutableListOf<MemoListEvent>()
        setEntityMemoFinishedListScaffold(
            pagingData = entityMemoPagingData(itemList = listOf(MemoListItem.Content(memo = memo))),
            onMemoListEvent = eventList::add,
        )
        waitUntilMemoIsDisplayed(title = DELETE_MEMO_TITLE)

        composeRule.onNodeWithText(DELETE_MEMO_TITLE).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        eventList.shouldContainExactly(MemoListEvent.SwipeDelete(id = memo.id))
    }

    @Test
    fun `TC-bd6997b9-d2e8-4e97-acbb-84fa17a7d124 상단 바 뒤로가기는 화면 닫기를 요청한다`() {
        val eventList = mutableListOf<EntityMemoFinishedListScaffoldEvent>()
        setEntityMemoFinishedListScaffold(onEvent = eventList::add)

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()

        eventList.shouldContainExactly(EntityMemoFinishedListScaffoldEvent.ClickNavigateUp)
    }

    private fun setEntityMemoFinishedListScaffold(
        pagingData: PagingData<MemoListItem> = PagingData.empty(),
        uiState: EntityMemoFinishedListUiState = EntityMemoFinishedListUiState(),
        onEvent: (EntityMemoFinishedListScaffoldEvent) -> Unit = {},
        onMemoListEvent: (MemoListEvent) -> Unit = {},
        uiStateProvider: () -> EntityMemoFinishedListUiState = { uiState },
        isTodayUpdated: Boolean = false,
    ) {
        val pagingDataFlow = MutableStateFlow(pagingData)

        composeRule.setContent {
            DiaryTheme {
                EntityMemoFinishedListScaffold(
                    memoListState = remember { MemoListState().apply { if (isTodayUpdated) updateToday() } },
                    snackbarHostState = remember { SnackbarHostState() },
                    memoPagingItems = pagingDataFlow.collectAsLazyPagingItems(),
                    onEvent = onEvent,
                    onMemoListEvent = onMemoListEvent,
                    emptyTitle = EMPTY_TITLE,
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
        const val EMPTY_TITLE = "EntityEmptyTitle"
        const val PAGING_ITEMS_TIMEOUT_MILLIS = 5_000L
        const val OTHER_ENTITY_NAME = "OtherEntityName"
        const val ENTITY_NAME = "EntityName"
        val LONG_ENTITY_NAME = "LongEntityName".repeat(20)
        const val UPDATED_ENTITY_NAME = "UpdatedEntityName"
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
