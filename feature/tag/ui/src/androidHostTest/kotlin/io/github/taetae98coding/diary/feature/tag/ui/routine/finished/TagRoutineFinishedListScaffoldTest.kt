package io.github.taetae98coding.diary.feature.tag.ui.routine.finished

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.empty.DIARY_EMPTY_BOX_TEST_TAG
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.routine.ROUTINE_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.routine.ROUTINE_PROGRESS_BAR_TEST_TAG
import io.github.taetae98coding.diary.compose.routine.RoutineListEvent
import io.github.taetae98coding.diary.compose.routine.SwipeRoutineCard
import io.github.taetae98coding.diary.compose.routine.rememberRoutineListState
import io.github.taetae98coding.diary.core.model.routine.RoutineCycle
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.feature.tag.ui.refreshableList
import io.github.taetae98coding.diary.feature.tag.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.feature.tag.ui.tagEntityLoadingPagingData
import io.github.taetae98coding.diary.feature.tag.ui.tagEntityPagingData
import io.github.taetae98coding.diary.feature.tag.ui.tagRoutine
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class TagRoutineFinishedListScaffoldTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-57bb6310-bd22-41c4-80c9-01217bd67075 각 루틴의 제목과 카드 문구 완료를 표시하고 합계와 달성률은 표시하지 않는다`() {
        val marchFirst = LocalDate(year = 2026, month = 3, day = 1)
        val titleList = listOf(WORKOUT_TITLE, RUNNING_TITLE, READING_TITLE)
        setScaffold(
            pagingData =
                tagEntityPagingData(
                    itemList =
                        listOf(
                            finishedRoutine(title = WORKOUT_TITLE, startDate = marchFirst, cycle = RoutineCycle.WEEKLY, unit = "회", goalAmount = 3, cycleAmount = 2),
                            finishedRoutine(title = RUNNING_TITLE, startDate = marchFirst, cycle = RoutineCycle.MONTHLY, unit = "m", cycleAmount = 5000),
                            finishedRoutine(title = READING_TITLE, startDate = LocalDate(year = 2026, month = 3, day = 21), goalAmount = 1),
                        ),
                ),
            today = LocalDate(year = 2026, month = 3, day = 20),
        )

        titleList.forEach { title ->
            composeRule.onNode(hasTestTag(ROUTINE_CARD_TEST_TAG) and hasText(title) and hasText(KOREAN_FINISHED_TEXT)).assertIsDisplayed()
        }
        composeRule.onAllNodes(hasText("%", substring = true)).assertCountEquals(0)
        composeRule.onAllNodes(hasText(KOREAN_WEEK_CYCLE_NAME, substring = true)).assertCountEquals(0)
        composeRule.onAllNodes(hasText(KOREAN_MONTH_CYCLE_NAME, substring = true)).assertCountEquals(0)
        composeRule.onAllNodesWithTag(ROUTINE_PROGRESS_BAR_TEST_TAG, useUnmergedTree = true).assertCountEquals(0)
    }

    @Test
    fun `TC-e778d66c-6d6b-45b9-897d-b2d127bb852f 진행 중인 완료된 루틴 카드에는 빠른 기록이 없고 선택과 다시 시작과 삭제를 할 수 있다`() {
        val item = finishedRoutine(title = FIRST_TITLE, goalAmount = 3)
        val eventList = mutableListOf<RoutineListEvent>()
        setScaffold(pagingData = tagEntityPagingData(itemList = listOf(item)), onRoutineListEvent = eventList::add, today = LocalDate(year = 2026, month = 3, day = 20))

        // 카드 안에는 카드 자신 말고 누를 수 있는 것이 없다.
        composeRule.onAllNodes(hasClickAction() and hasAnyAncestor(hasTestTag(ROUTINE_CARD_TEST_TAG)), useUnmergedTree = true).assertCountEquals(0)
        composeRule.onNodeWithTag(ROUTINE_CARD_TEST_TAG).performTouchInput { longClick() }
        composeRule.waitForIdle()
        composeRule.onAllNodes(isDialog()).assertCountEquals(0)

        composeRule.onNodeWithTag(ROUTINE_CARD_TEST_TAG).performClick()
        composeRule.onNodeWithTag(ROUTINE_CARD_TEST_TAG).performTouchInput { swipeRight() }
        composeRule.mainClock.advanceTimeBy(SWIPE_RESET_MILLIS)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(ROUTINE_CARD_TEST_TAG).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        eventList.filterIsInstance<RoutineListEvent.ClickRoutine>().distinct() shouldBe listOf(RoutineListEvent.ClickRoutine(id = item.routine.id))
        eventList.filterIsInstance<RoutineListEvent.SwipeRestart>() shouldBe listOf(RoutineListEvent.SwipeRestart(id = item.routine.id))
        eventList.filterIsInstance<RoutineListEvent.SwipeDelete>() shouldBe listOf(RoutineListEvent.SwipeDelete(id = item.routine.id))
        eventList.filterIsInstance<RoutineListEvent.SwipeFinish>() shouldBe emptyList()
    }

    @Test
    fun `TC-3cf8f51e-18ca-439d-80b8-985a8ba5d26f 이 화면은 정렬 선택을 제공하지 않는다`() {
        setScaffold(pagingData = tagEntityPagingData(itemList = listOf(finishedRoutine(title = FIRST_TITLE))))

        composeRule.onNodeWithContentDescription(DEFAULT_SORT_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-156d6fab-eebe-40ad-aef5-54f471c7e311 상단 바에 대상 태그 표시와 완료된 루틴 목록 표시를 함께 제공한다`() {
        setScaffold(uiState = TagRoutineFinishedListUiState(title = TAG_TITLE))

        composeRule.onNodeWithText(TAG_TITLE).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_SUBTITLE).assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-156d6fab-eebe-40ad-aef5-54f471c7e311 한국어 환경에서 완료된 루틴 목록 표시는 완료된 루틴이다`() {
        setScaffold(uiState = TagRoutineFinishedListUiState(title = TAG_TITLE))

        composeRule.onNodeWithText(TAG_TITLE).assertIsDisplayed()
        composeRule.onNodeWithText(KOREAN_SUBTITLE).assertIsDisplayed()
    }

    @Test
    fun `TC-804fb661-78ff-462e-bcf9-66eb81b795ec 태그 표시가 비어 있어도 완료된 루틴 목록 표시와 뒤로가기와 목록을 표시한다`() {
        setScaffold(
            uiState = TagRoutineFinishedListUiState(),
            pagingData = tagEntityPagingData(itemList = listOf(finishedRoutine(title = FIRST_TITLE))),
        )

        composeRule.onNodeWithText(TAG_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_SUBTITLE).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithText(FIRST_TITLE).assertIsDisplayed()
    }

    @Test
    fun `TC-8715c030-ff56-4724-ad52-b39984842c13 태그 표시가 바뀌면 바뀐 표시를 사용한다`() {
        val uiStateFlow = MutableStateFlow(TagRoutineFinishedListUiState(title = TAG_TITLE))
        setScaffoldFlow(uiStateFlow = uiStateFlow)
        composeRule.onNodeWithText(TAG_TITLE).assertIsDisplayed()

        composeRule.runOnIdle { uiStateFlow.value = TagRoutineFinishedListUiState(title = UPDATED_TAG_TITLE) }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(UPDATED_TAG_TITLE).assertIsDisplayed()
        composeRule.onNodeWithText(TAG_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-f8ffd099-14c4-4412-a1b1-4a624c56c9d5 자리 표시 상태의 루틴은 선택하거나 다시 시작하거나 삭제할 수 없다`() {
        val eventList = mutableListOf<RoutineListEvent>()
        // 목록은 아직 준비되지 않은 자리를 루틴 없이 같은 카드로 그린다.
        composeRule.setContent {
            DiaryTheme {
                SwipeRoutineCard(
                    onEvent = eventList::add,
                    modifier = Modifier.fillMaxWidth(),
                    summary = null,
                )
            }
        }

        composeRule.onNodeWithTag(ROUTINE_CARD_TEST_TAG).assertIsNotEnabled()
        composeRule.onNodeWithTag(ROUTINE_CARD_TEST_TAG).performClick()
        composeRule.onNodeWithTag(ROUTINE_CARD_TEST_TAG).performTouchInput { swipeRight() }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(ROUTINE_CARD_TEST_TAG).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        eventList shouldBe emptyList()
        composeRule.onNodeWithTag(ROUTINE_CARD_TEST_TAG).assertIsDisplayed()
    }

    @Test
    fun `TC-5a72635e-bc1e-417d-be4c-2ffd293c95fb 최초 조회 실패는 루틴 없이 오류와 재시도를 표시하지 않는다`() {
        setScaffold(
            pagingData =
                tagEntityPagingData(
                    itemList = emptyList(),
                    refresh = LoadState.Error(IllegalStateException("Refresh failed")),
                ),
        )

        composeRule.onAllNodesWithTag(ROUTINE_CARD_TEST_TAG).assertCountEquals(0)
        composeRule.onNodeWithText(DEFAULT_ERROR_TEXT).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_RETRY_TEXT).assertDoesNotExist()
    }

    @Test
    fun `TC-5a72635e-bc1e-417d-be4c-2ffd293c95fb 추가 조회 실패는 표시된 루틴을 유지하고 오류와 재시도를 표시하지 않는다`() {
        setScaffold(
            pagingData =
                tagEntityPagingData(
                    itemList = listOf(finishedRoutine(title = FIRST_TITLE)),
                    append = LoadState.Error(IllegalStateException("Append failed")),
                ),
        )

        composeRule.onNodeWithText(FIRST_TITLE).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_ERROR_TEXT).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_RETRY_TEXT).assertDoesNotExist()
    }

    @Test
    fun `TC-5a72635e-bc1e-417d-be4c-2ffd293c95fb 다시 조회가 실패하면 이전 루틴을 남기지 않고 오류와 재시도를 표시하지 않는다`() {
        val pagingDataFlow = MutableStateFlow(tagEntityPagingData(itemList = listOf(finishedRoutine(title = FIRST_TITLE))))
        setScaffoldFlow(pagingDataFlow = pagingDataFlow)
        composeRule.onNodeWithText(FIRST_TITLE).assertIsDisplayed()

        composeRule.runOnIdle {
            pagingDataFlow.value = tagEntityPagingData(itemList = emptyList(), refresh = LoadState.Error(IllegalStateException("Refresh failed")))
        }
        composeRule.waitForIdle()

        composeRule.onAllNodesWithTag(ROUTINE_CARD_TEST_TAG).assertCountEquals(0)
        composeRule.onNodeWithText(DEFAULT_ERROR_TEXT).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_RETRY_TEXT).assertDoesNotExist()
    }

    @Test
    fun `TC-dc1592b7-b580-439a-abc4-708eb4a50668 TC-6defde2b-bec0-4a63-8207-c9e9e8f7aefa 이 화면에서는 루틴을 추가할 수 없다`() {
        setScaffold(pagingData = tagEntityPagingData(itemList = listOf(finishedRoutine(title = FIRST_TITLE))))

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).assertDoesNotExist()
        // 화면에서 누를 수 있는 것은 뒤로가기와 루틴 카드뿐이다.
        composeRule.onAllNodes(hasClickAction()).assertCountEquals(2)
    }

    @Test
    fun `TC-0d21ba4a-a518-45fb-8bb0-63fe43d75742 완료된 루틴이 없으면 빈 상태 안내를 표시하고 루틴 추가를 권하지 않는다`() {
        setScaffold(pagingData = tagEntityPagingData(itemList = emptyList()))

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_ADD_GUIDE).assertDoesNotExist()
        composeRule.onAllNodesWithTag(ROUTINE_CARD_TEST_TAG).assertCountEquals(0)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-0d21ba4a-a518-45fb-8bb0-63fe43d75742 한국어 환경에서 빈 상태 안내는 이 태그에서 완료한 루틴이 없습니다이다`() {
        setScaffold(pagingData = tagEntityPagingData(itemList = emptyList()))

        composeRule.onNodeWithText(KOREAN_EMPTY_TITLE).assertIsDisplayed()
    }

    @Test
    fun `TC-5dce0c3b-ad47-4d92-a5ba-783702c7f1c9 목록을 준비하는 동안에는 빈 상태 안내를 표시하지 않는다`() {
        setScaffold(pagingData = tagEntityLoadingPagingData())

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `TC-6eb5bdb0-a990-42ec-a61b-4797996c2479 대상 태그를 조회하지 못해도 빈 상태 안내를 표시한다`() {
        setScaffold(
            uiState = TagRoutineFinishedListUiState(),
            pagingData = tagEntityPagingData(itemList = emptyList()),
        )

        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertIsDisplayed()
    }

    @Test
    fun `TC-0661f905-e083-4a04-b9f6-a29d4fdcd207 루틴 카드를 선택하면 그 루틴의 상세 이동을 요청한다`() {
        val item = finishedRoutine(title = FIRST_TITLE)
        val eventList = mutableListOf<RoutineListEvent>()
        setScaffold(pagingData = tagEntityPagingData(itemList = listOf(item)), onRoutineListEvent = eventList::add)

        composeRule.onNodeWithText(FIRST_TITLE).performClick()

        eventList shouldBe listOf(RoutineListEvent.ClickRoutine(id = item.routine.id))
    }

    @Test
    fun `TC-ec07790d-e535-491c-9bf1-2cd0a6651514 상단 바 뒤로가기는 화면 닫기를 요청한다`() {
        val eventList = mutableListOf<TagRoutineFinishedListScaffoldEvent>()
        setScaffold(onEvent = eventList::add)

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()

        eventList shouldBe listOf(TagRoutineFinishedListScaffoldEvent.ClickNavigateUp)
    }

    @Test
    fun `TC-ae30f4e2-3f16-45d8-8969-7a43f8cddc57 목록은 전달된 순서 그대로 한 줄에 하나씩 표시한다`() {
        val titleList = listOf(THIRD_ORDER_TITLE, FOURTH_ORDER_TITLE, FIRST_ORDER_TITLE, SECOND_ORDER_TITLE)
        setScaffold(pagingData = tagEntityPagingData(itemList = titleList.map { title -> finishedRoutine(title = title) }))

        val topList =
            titleList.map { title ->
                composeRule
                    .onNodeWithText(title)
                    .fetchSemanticsNode()
                    .boundsInRoot
                    .top
            }

        topList shouldBe topList.sorted()
        topList.distinct().size shouldBe titleList.size
    }

    @Test
    fun `목록을 당기면 새로고침을 요청한다`() {
        val eventList = mutableListOf<TagRoutineFinishedListScaffoldEvent>()
        setScaffold(pagingData = tagEntityPagingData(itemList = listOf(finishedRoutine(title = FIRST_TITLE))), onEvent = eventList::add)

        composeRule.refreshableList().performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        eventList shouldBe listOf(TagRoutineFinishedListScaffoldEvent.Refresh)
    }

    private fun finishedRoutine(
        title: String,
        startDate: LocalDate = LocalDate(year = 2000, month = 1, day = 1),
        cycle: RoutineCycle = RoutineCycle.DAILY,
        unit: String = "회",
        goalAmount: Int? = null,
        cycleAmount: Long = 0,
    ): RoutineSummary = tagRoutine(title = title, startDate = startDate, isFinished = true, cycle = cycle, unit = unit, goalAmount = goalAmount, cycleAmount = cycleAmount)

    private fun setScaffold(
        uiState: TagRoutineFinishedListUiState = TagRoutineFinishedListUiState(title = TAG_TITLE),
        pagingData: PagingData<RoutineSummary> = PagingData.empty(),
        onEvent: (TagRoutineFinishedListScaffoldEvent) -> Unit = {},
        onRoutineListEvent: (RoutineListEvent) -> Unit = {},
        today: LocalDate? = null,
    ) {
        setScaffoldFlow(
            uiStateFlow = MutableStateFlow(uiState),
            pagingDataFlow = MutableStateFlow(pagingData),
            onEvent = onEvent,
            onRoutineListEvent = onRoutineListEvent,
            today = today,
        )
    }

    private fun setScaffoldFlow(
        uiStateFlow: MutableStateFlow<TagRoutineFinishedListUiState> = MutableStateFlow(TagRoutineFinishedListUiState(title = TAG_TITLE)),
        pagingDataFlow: MutableStateFlow<PagingData<RoutineSummary>> = MutableStateFlow(PagingData.empty()),
        onEvent: (TagRoutineFinishedListScaffoldEvent) -> Unit = {},
        onRoutineListEvent: (RoutineListEvent) -> Unit = {},
        today: LocalDate? = null,
    ) {
        composeRule.setContent {
            val uiState by uiStateFlow.collectAsStateWithLifecycle()

            DiaryTheme {
                TagRoutineFinishedListScaffold(
                    onEvent = onEvent,
                    onRoutineListEvent = onRoutineListEvent,
                    listState = rememberRoutineListState(initialToday = today),
                    routinePagingItems = pagingDataFlow.collectAsLazyPagingItems(),
                    uiStateProvider = { uiState },
                )
            }
        }
        composeRule.waitForIdle()
    }

    private companion object {
        const val FIRST_TITLE = "AlphaRoutine"
        const val FIRST_ORDER_TITLE = "가"
        const val SECOND_ORDER_TITLE = "나"
        const val THIRD_ORDER_TITLE = "다"
        const val FOURTH_ORDER_TITLE = "라"
        const val WORKOUT_TITLE = "운동"
        const val RUNNING_TITLE = "달리기"
        const val READING_TITLE = "독서"
        const val KOREAN_FINISHED_TEXT = "완료"
        const val KOREAN_WEEK_CYCLE_NAME = "이번 주"
        const val KOREAN_MONTH_CYCLE_NAME = "이번 달"
        const val TAG_TITLE = "📌 회사"
        const val UPDATED_TAG_TITLE = "📎 집"
        const val DEFAULT_SUBTITLE = "Finished Routines"
        const val KOREAN_SUBTITLE = "완료된 루틴"
        const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        const val DEFAULT_ADD_BUTTON_DESCRIPTION = "Add routine"
        const val DEFAULT_EMPTY_TITLE = "No finished routines for this tag"
        const val KOREAN_EMPTY_TITLE = "이 태그에서 완료한 루틴이 없습니다"
        const val DEFAULT_ADD_GUIDE = "Use the add button to create a routine."
        const val DEFAULT_ERROR_TEXT = "Error"
        const val DEFAULT_RETRY_TEXT = "Retry"
        const val DEFAULT_SORT_DESCRIPTION = "List sort"
        const val SWIPE_RESET_MILLIS = 2_000L
    }
}
