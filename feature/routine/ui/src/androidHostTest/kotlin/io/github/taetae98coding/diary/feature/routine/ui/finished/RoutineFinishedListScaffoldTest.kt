package io.github.taetae98coding.diary.feature.routine.ui.finished

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.routine.ROUTINE_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.routine.ROUTINE_PROGRESS_BAR_TEST_TAG
import io.github.taetae98coding.diary.compose.routine.RoutineListEvent
import io.github.taetae98coding.diary.compose.routine.SwipeRoutineCard
import io.github.taetae98coding.diary.compose.routine.rememberRoutineListState
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.ROUTINE_HOME_CALENDAR_RECORD_TEST_TAG
import io.github.taetae98coding.diary.feature.routine.ui.home.record.ROUTINE_QUICK_RECORD_BUTTON_TEST_TAG
import io.github.taetae98coding.diary.feature.routine.ui.resetAndroidUiDispatcher
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// 완료된 루틴 목록 화면이 보여 주는 것과 제공하지 않는 동작을 한국어 환경의 문구로 확인한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "ko-w411dp-h891dp")
class RoutineFinishedListScaffoldTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-7925a5af-53c7-43c9-afc3-78ea03a18a56 카드에는 제목과 카드 문구 완료만 표시하고 합계와 달성률은 표시하지 않는다`() {
        val summaryList =
            listOf(
                summary(title = "목표 있는 루틴", startDate = LocalDate(2026, 7, 1), goalAmount = 2000, cycleAmount = 1500),
                summary(title = "목표 없는 루틴", startDate = LocalDate(2026, 7, 1), goalAmount = null, cycleAmount = 3),
                summary(title = "시작 전인 루틴", startDate = LocalDate(2026, 7, 16), goalAmount = null, cycleAmount = 0),
            )

        setScaffold(pagingData = flowOf(routineFinishedListPagingDataOf(summaryList)))
        composeRule.waitUntilRoutineFinishedListExists(routineFinishedListCard("목표 있는 루틴"))

        composeRule.routineFinishedListCardTextList() shouldBe summaryList.map { summary -> listOf(summary.routine.detail.title, FINISHED_TEXT) }
        composeRule.onAllNodes(hasTestTag(ROUTINE_PROGRESS_BAR_TEST_TAG), useUnmergedTree = true).assertCountEquals(0)
        composeRule.routineFinishedListCardTextList().flatten().contains(NOT_STARTED_TEXT) shouldBe false
        composeRule.onAllNodesWithText("%", substring = true).assertCountEquals(0)
    }

    @Test
    fun `TC-0802399f-36cc-45f5-818f-f304d4728122 카드에는 빠른 기록을 제공하지 않는다`() {
        setScaffold(pagingData = flowOf(routineFinishedListPagingDataOf(listOf(summary(title = ROUTINE_TITLE, startDate = LocalDate(2026, 7, 1))))))
        composeRule.waitUntilRoutineFinishedListExists(routineFinishedListCard(ROUTINE_TITLE))

        composeRule.onAllNodes(hasTestTag(ROUTINE_QUICK_RECORD_BUTTON_TEST_TAG), useUnmergedTree = true).assertCountEquals(0)
        composeRule.onAllNodesWithContentDescription(QUICK_RECORD_SUFFIX, substring = true).assertCountEquals(0)
    }

    @Test
    fun `TC-01d798bc-77d8-4bb8-b3a7-1f3a6461c2a6 목록에는 제목과 카드 문구 밖의 루틴 정보를 표시하지 않는다`() {
        val routine =
            routineFinishedListRoutine(
                title = ROUTINE_TITLE,
                startDate = LocalDate(2026, 7, 1),
                unit = "ml",
                goalAmount = 2000,
                amountPerRecord = 250,
                description = "하루에 여덟 잔",
            )

        setScaffold(pagingData = flowOf(routineFinishedListPagingDataOf(listOf(RoutineSummary(routine = routine, cycleAmount = 500)))))
        composeRule.waitUntilRoutineFinishedListExists(routineFinishedListCard(ROUTINE_TITLE))

        composeRule.routineFinishedListCardTextList() shouldBe listOf(listOf(ROUTINE_TITLE, FINISHED_TEXT))
    }

    @Test
    fun `TC-40ed6523-5674-43ff-a795-c899af8aa40a 이 화면에서는 루틴을 추가할 수 없다`() {
        setScaffold(pagingData = flowOf(routineFinishedListPagingDataOf(listOf(summary(title = ROUTINE_TITLE, startDate = LocalDate(2026, 7, 1))))))
        composeRule.waitUntilRoutineFinishedListExists(routineFinishedListCard(ROUTINE_TITLE))

        composeRule.onAllNodesWithContentDescription(ADD_DESCRIPTION).assertCountEquals(0)
        assertOnlyNavigateUpAndCardsAreClickable(cardCount = 1)
    }

    @Test
    fun `TC-390b1383-311d-4004-b4bd-e8c92e9628bb 이 화면에는 태그 필터를 제공하지 않는다`() {
        setScaffold(pagingData = flowOf(routineFinishedListPagingDataOf(listOf(summary(title = ROUTINE_TITLE, startDate = LocalDate(2026, 7, 1))))))
        composeRule.waitUntilRoutineFinishedListExists(routineFinishedListCard(ROUTINE_TITLE))

        composeRule.onAllNodesWithContentDescription(FILTER_DESCRIPTION).assertCountEquals(0)
        assertOnlyNavigateUpAndCardsAreClickable(cardCount = 1)
    }

    @Test
    fun `TC-bf888c96-a28a-496f-8c6b-afe6a8d327b8 이 화면에는 보기 전환과 달력 보기를 제공하지 않는다`() {
        setScaffold(pagingData = flowOf(routineFinishedListPagingDataOf(listOf(summary(title = ROUTINE_TITLE, startDate = LocalDate(2026, 7, 1), cycleAmount = 3)))))
        composeRule.waitUntilRoutineFinishedListExists(routineFinishedListCard(ROUTINE_TITLE))

        composeRule.onAllNodesWithContentDescription(SHOW_CALENDAR_DESCRIPTION).assertCountEquals(0)
        composeRule.onAllNodesWithContentDescription(SHOW_LIST_DESCRIPTION).assertCountEquals(0)
        composeRule.onAllNodes(hasTestTag(ROUTINE_HOME_CALENDAR_RECORD_TEST_TAG), useUnmergedTree = true).assertCountEquals(0)
        composeRule.onNode(routineFinishedListCard(ROUTINE_TITLE)).assertExists()
        assertOnlyNavigateUpAndCardsAreClickable(cardCount = 1)
    }

    @Test
    fun `TC-5c951be0-2ab5-4a2c-903c-28cdcd1a36a8 이 화면에는 정렬 선택을 제공하지 않는다`() {
        val summaryList = listOf("가", "나", "다").map { title -> summary(title = title, startDate = LocalDate(2026, 7, 1)) }
        setScaffold(pagingData = flowOf(routineFinishedListPagingDataOf(summaryList)))
        composeRule.waitUntilRoutineFinishedListExists(routineFinishedListCard("가"))

        composeRule.onAllNodesWithContentDescription(SORT_DESCRIPTION).assertCountEquals(0)
        SORT_LABEL_LIST.forEach { label -> composeRule.onAllNodesWithText(label).assertCountEquals(0) }
        assertOnlyNavigateUpAndCardsAreClickable(cardCount = summaryList.size)
    }

    @Test
    fun `TC-045005cf-6cff-4ff2-a7ef-8449cf683b20 완료된 루틴이 없으면 빈 상태 안내를 표시한다`() {
        setScaffold(pagingData = flowOf(routineFinishedListPagingDataOf(emptyList())))

        composeRule.waitUntilRoutineFinishedListExists(hasText(EMPTY_TITLE))
        composeRule.onAllNodes(hasTestTag(ROUTINE_CARD_TEST_TAG)).assertCountEquals(0)
        composeRule.onAllNodesWithText(ADD_SUGGESTION).assertCountEquals(0)
        composeRule.onAllNodesWithText(HOME_EMPTY_TITLE).assertCountEquals(0)
    }

    @Test
    fun `TC-b631ee1e-0e11-47f2-951c-39ef495fb19d 목록을 준비하는 동안에는 빈 상태 안내를 표시하지 않는다`() {
        val loading =
            PagingData.from(
                data = emptyList<RoutineSummary>(),
                sourceLoadStates =
                    LoadStates(
                        refresh = LoadState.Loading,
                        prepend = LoadState.NotLoading(endOfPaginationReached = false),
                        append = LoadState.NotLoading(endOfPaginationReached = false),
                    ),
            )
        setScaffold(pagingData = MutableStateFlow(loading))
        composeRule.waitForIdle()

        composeRule.onAllNodesWithText(EMPTY_TITLE).assertCountEquals(0)
        composeRule.onAllNodes(hasTestTag(ROUTINE_CARD_TEST_TAG)).assertCountEquals(0)
    }

    @Test
    fun `TC-0f0c4820-3db5-456d-9932-6f8945c81b79 이어서 불러오기에 실패해도 오류 안내 없이 앞서 불러온 루틴을 그대로 표시한다`() {
        val summaryList = listOf("가", "나").map { title -> summary(title = title, startDate = LocalDate(2026, 7, 1)) }
        val pagingData =
            PagingData.from(
                data = summaryList,
                sourceLoadStates =
                    LoadStates(
                        refresh = LoadState.NotLoading(endOfPaginationReached = false),
                        prepend = LoadState.NotLoading(endOfPaginationReached = true),
                        append = LoadState.Error(IllegalStateException("저장하거나 불러오지 못함")),
                    ),
            )

        setScaffold(pagingData = flowOf(pagingData))
        composeRule.waitUntilRoutineFinishedListExists(routineFinishedListCard("가"))

        composeRule.routineFinishedListCardTitleList() shouldBe listOf("가", "나")
        composeRule.onAllNodesWithText(EMPTY_TITLE).assertCountEquals(0)
        assertOnlyNavigateUpAndCardsAreClickable(cardCount = summaryList.size)
    }

    @Test
    fun `TC-0f0c4820-3db5-456d-9932-6f8945c81b79 불러오지 못해 보이는 루틴이 없으면 오류 안내 없이 빈 상태 안내를 표시한다`() {
        val pagingData = MutableStateFlow(routineFinishedListPagingDataOf(listOf(summary(title = ROUTINE_TITLE, startDate = LocalDate(2026, 7, 1)))))
        setScaffold(pagingData = pagingData)
        composeRule.waitUntilRoutineFinishedListExists(routineFinishedListCard(ROUTINE_TITLE))

        // 조회에 실패하면 완료된 루틴 목록은 불러오기를 마친 빈 목록을 받는다.
        composeRule.runOnIdle { pagingData.value = routineFinishedListPagingDataOf(emptyList()) }

        composeRule.waitUntilRoutineFinishedListExists(hasText(EMPTY_TITLE))
        composeRule.onAllNodes(hasTestTag(ROUTINE_CARD_TEST_TAG)).assertCountEquals(0)
        assertOnlyNavigateUpAndCardsAreClickable(cardCount = 0)
    }

    @Test
    fun `TC-42b779be-6679-4641-a194-4f99d8368a45 아직 준비되지 않은 루틴은 선택하거나 상태를 바꿀 수 없다`() {
        val eventList = mutableListOf<RoutineListEvent>()
        composeRule.setContent {
            DiaryTheme {
                SwipeRoutineCard(
                    onEvent = eventList::add,
                    modifier = Modifier.fillMaxWidth(),
                    state = rememberRoutineListState(initialToday = TODAY),
                    summary = null,
                )
            }
        }

        composeRule.onNodeWithTag(ROUTINE_CARD_TEST_TAG).performClick()
        composeRule.onNodeWithTag(ROUTINE_CARD_TEST_TAG).performTouchInput { swipeRight() }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(ROUTINE_CARD_TEST_TAG).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        eventList.shouldBeEmpty()
    }

    // 화면에서 누를 수 있는 것이 뒤로가기와 루틴 카드뿐인지 확인한다.
    private fun assertOnlyNavigateUpAndCardsAreClickable(cardCount: Int) {
        composeRule.onNode(hasContentDescription(NAVIGATE_UP_DESCRIPTION) and hasClickAction()).assertExists()
        composeRule.onAllNodes(hasClickAction()).assertCountEquals(cardCount + 1)
        composeRule.onNodeWithText(TITLE).assertExists()
    }

    private fun setScaffold(pagingData: Flow<PagingData<RoutineSummary>>) {
        composeRule.setContent {
            DiaryTheme {
                RoutineFinishedListScaffold(
                    onEvent = {},
                    onRoutineListEvent = {},
                    listState = rememberRoutineListState(initialToday = TODAY),
                    routinePagingItems = pagingData.collectAsLazyPagingItems(),
                )
            }
        }
    }

    private companion object {
        val TODAY: LocalDate = LocalDate(2026, 7, 15)
        const val ROUTINE_TITLE = "물 마시기"
        const val TITLE = "완료된 루틴"
        const val FINISHED_TEXT = "완료"
        const val NOT_STARTED_TEXT = "시작 전"
        const val EMPTY_TITLE = "완료한 루틴이 없습니다"
        const val HOME_EMPTY_TITLE = "아직 루틴이 없습니다"
        const val ADD_SUGGESTION = "추가 버튼으로 새 루틴을 만들 수 있습니다"
        const val NAVIGATE_UP_DESCRIPTION = "뒤로가기"
        const val ADD_DESCRIPTION = "루틴 추가"
        const val FILTER_DESCRIPTION = "필터"
        const val SHOW_CALENDAR_DESCRIPTION = "달력으로 보기"
        const val SHOW_LIST_DESCRIPTION = "목록으로 보기"
        const val SORT_DESCRIPTION = "목록 정렬"
        const val QUICK_RECORD_SUFFIX = " 기록"
        val SORT_LABEL_LIST: List<String> = listOf("기본순", "이름순", "최근 수정순", "제목순")

        fun summary(
            title: String,
            startDate: LocalDate,
            goalAmount: Int? = null,
            cycleAmount: Long = 0,
        ): RoutineSummary =
            RoutineSummary(
                routine = routineFinishedListRoutine(title = title, startDate = startDate, goalAmount = goalAmount),
                cycleAmount = cycleAmount,
            )
    }
}
