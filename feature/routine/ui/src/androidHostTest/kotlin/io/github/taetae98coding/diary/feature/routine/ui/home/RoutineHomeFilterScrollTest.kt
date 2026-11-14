package io.github.taetae98coding.diary.feature.routine.ui.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToIndex
import androidx.paging.PagingData
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.core.model.routine.RoutineViewScope
import io.github.taetae98coding.diary.feature.routine.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.feature.routine.ui.routineHomeCalendarViewModel
import io.github.taetae98coding.diary.feature.routine.ui.routineHomeHolidayViewModel
import io.github.taetae98coding.diary.feature.routine.ui.routineSyncRefreshViewModel
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

// 필터 선택과 조회 결과는 저장소에서 오는 값이므로 그 흐름만 테스트가 바꾼다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class RoutineHomeFilterScrollTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-cb3bc7ed-8b27-48ae-8b61-72f0a3923dcd 선택되어 있지 않은 태그를 선택하면 목록을 처음부터 다시 본다`() {
        val tagId = Arb.kotlinUuid().next()
        val environment = setScrolledRoutineHome()

        changeQuery(environment = environment, filterUiState = appliedFilter(setOf(tagId)))

        assertBackToTop(environment)
    }

    @Test
    fun `TC-cb3bc7ed-8b27-48ae-8b61-72f0a3923dcd 선택되어 있는 태그의 선택을 해제하면 목록을 처음부터 다시 본다`() {
        val firstTagId = Arb.kotlinUuid().next()
        val secondTagId = Arb.kotlinUuid().next()
        val environment = setScrolledRoutineHome(filterUiState = appliedFilter(setOf(firstTagId, secondTagId)))

        changeQuery(environment = environment, filterUiState = appliedFilter(setOf(firstTagId)))

        assertBackToTop(environment)
    }

    @Test
    fun `TC-cb3bc7ed-8b27-48ae-8b61-72f0a3923dcd 선택을 전체 해제하면 목록을 처음부터 다시 본다`() {
        val tagId = Arb.kotlinUuid().next()
        val environment = setScrolledRoutineHome(filterUiState = appliedFilter(setOf(tagId)))

        changeQuery(environment = environment, filterUiState = RoutineHomeScaffoldFilterUiState())

        assertBackToTop(environment)
    }

    @Test
    fun `TC-7611b734-5a81-486f-b5d5-b5c1e2004421 보기 범위를 오늘 할 루틴에서 모든 루틴으로 바꾸면 목록을 처음부터 다시 본다`() {
        val environment = setScrolledRoutineHome(filterUiState = RoutineHomeScaffoldFilterUiState(viewScope = RoutineViewScope.TODAY, hasUnfinishedRoutine = true))

        changeQuery(environment = environment, filterUiState = RoutineHomeScaffoldFilterUiState(viewScope = RoutineViewScope.ALL, hasUnfinishedRoutine = true))

        assertBackToTop(environment)
    }

    @Test
    fun `TC-7611b734-5a81-486f-b5d5-b5c1e2004421 보기 범위를 모든 루틴에서 오늘 할 루틴으로 바꾸면 목록을 처음부터 다시 본다`() {
        val environment = setScrolledRoutineHome(filterUiState = RoutineHomeScaffoldFilterUiState(viewScope = RoutineViewScope.ALL, hasUnfinishedRoutine = true))

        changeQuery(environment = environment, filterUiState = RoutineHomeScaffoldFilterUiState(viewScope = RoutineViewScope.TODAY, hasUnfinishedRoutine = true))

        assertBackToTop(environment)
    }

    @Test
    fun `TC-8389da95-7521-4222-9754-2e7007fb3d13 오늘 할 루틴을 다시 골라도 목록과 보던 자리가 바뀌지 않는다`() {
        assertPositionKeptAfterSameViewScope(viewScope = RoutineViewScope.TODAY)
    }

    @Test
    fun `TC-8389da95-7521-4222-9754-2e7007fb3d13 모든 루틴을 다시 골라도 목록과 보던 자리가 바뀌지 않는다`() {
        assertPositionKeptAfterSameViewScope(viewScope = RoutineViewScope.ALL)
    }

    // 같은 값을 다시 저장하면 저장된 선택이 그대로라 같은 상태와 같은 목록이 다시 전달된다.
    private fun assertPositionKeptAfterSameViewScope(viewScope: RoutineViewScope) {
        val filterUiState = RoutineHomeScaffoldFilterUiState(viewScope = viewScope, hasUnfinishedRoutine = true)
        val environment = setScrolledRoutineHome(filterUiState = filterUiState)

        composeRule.runOnIdle { environment.filterUiStateFlow.value = filterUiState.copy() }
        composeRule.waitForIdle()
        composeRule.runOnIdle { environment.pagingDataFlow.value = filterCaseRoutinePagingDataOf(environment.summaryList) }
        composeRule.waitForIdle()

        assertPositionKept(environment)
    }

    // 오늘을 다시 정하면 필터 선택은 그대로인 채 그 오늘로 좁힌 목록이 전달된다. 좁힌 목록에도 보던 루틴이 남아 있다.
    @Test
    fun `TC-cc04f0c1-ae90-400a-8dee-87a4fc434d7f 오늘을 다시 정해 목록에 놓인 루틴이 달라져도 보던 목록 위치를 유지한다`() {
        val environment = setScrolledRoutineHome(filterUiState = RoutineHomeScaffoldFilterUiState(viewScope = RoutineViewScope.TODAY, hasUnfinishedRoutine = true))
        val removedTitle = environment.titleAt(1)

        composeRule.runOnIdle { environment.pagingDataFlow.value = filterCaseRoutinePagingDataOf(environment.narrowedList) }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(environment.titleAt(SCROLLED_INDEX)).assertIsDisplayed()
        composeRule.onNodeWithText(environment.titleAt(0)).assertDoesNotExist()
        environment.narrowedList.none { summary -> summary.routine.detail.title == removedTitle } shouldBe true
    }

    @Test
    fun `TC-3361901a-d462-4bc3-80b2-ac9cd72098c1 좁힌 결과가 놓이기 전에는 목록 위치를 유지한다`() {
        val environment = setScrolledRoutineHome()

        composeRule.runOnIdle { environment.filterUiStateFlow.value = appliedFilter(setOf(Arb.kotlinUuid().next())) }
        composeRule.waitForIdle()
        composeRule.runOnIdle { environment.pagingDataFlow.value = filterCaseRoutinePagingDataOf(environment.summaryList, isRefreshing = true) }
        composeRule.waitForIdle()

        assertPositionKept(environment)
    }

    @Test
    fun `TC-1a9c1b54-0647-48d1-92b5-b86b387667e4 노출 대상 루틴이 추가되어도 목록 위치를 유지한다`() {
        val environment = setScrolledRoutineHome()

        composeRule.runOnIdle { environment.pagingDataFlow.value = filterCaseRoutinePagingDataOf(listOf(addedSummary()) + environment.summaryList) }
        composeRule.waitForIdle()

        assertPositionKept(environment)
    }

    @Test
    fun `TC-1a9c1b54-0647-48d1-92b5-b86b387667e4 보고 있던 자리 밖의 루틴이 삭제되어도 목록 위치를 유지한다`() {
        val environment = setScrolledRoutineHome()

        composeRule.runOnIdle { environment.pagingDataFlow.value = filterCaseRoutinePagingDataOf(environment.summaryList.drop(1)) }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(environment.titleAt(SCROLLED_INDEX)).assertIsDisplayed()
        composeRule.onNodeWithText(environment.titleAt(1)).assertDoesNotExist()
    }

    // 빠른 기록이 저장되면 그 카드의 합계만 바뀐 목록이 다시 전달된다.
    @Test
    fun `TC-1a9c1b54-0647-48d1-92b5-b86b387667e4 보고 있는 카드에서 빠른 기록을 넣어도 목록 위치를 유지한다`() {
        val environment = setScrolledRoutineHome()
        val recordedList =
            environment.summaryList.mapIndexed { index, summary ->
                if (index == SCROLLED_INDEX) summary.copy(cycleAmount = summary.cycleAmount + summary.routine.detail.amountPerRecord) else summary
            }

        composeRule.runOnIdle { environment.pagingDataFlow.value = filterCaseRoutinePagingDataOf(recordedList) }
        composeRule.waitForIdle()

        assertPositionKept(environment)
    }

    @Test
    fun `TC-1a9c1b54-0647-48d1-92b5-b86b387667e4 서버에서 내려받은 루틴과 기록이 반영되어도 목록 위치를 유지한다`() {
        val environment = setScrolledRoutineHome()
        val syncedList =
            environment.summaryList.mapIndexed { index, summary ->
                if (index == 1) {
                    summary.copy(
                        routine =
                            summary.routine.copy(
                                detail =
                                    summary.routine.detail.copy(
                                        color =
                                            summary.routine.detail.color
                                                .inv(),
                                    ),
                            ),
                        cycleAmount = summary.cycleAmount + 1L,
                    )
                } else {
                    summary
                }
            } + addedSummary()

        composeRule.runOnIdle { environment.pagingDataFlow.value = filterCaseRoutinePagingDataOf(syncedList) }
        composeRule.waitForIdle()

        assertPositionKept(environment)
    }

    @Test
    fun `TC-9cd1164d-7349-425c-bab0-83bf20d6195a 저장된 필터를 처음 불러와 반영해도 목록 위치를 유지한다`() {
        val environment = setScrolledRoutineHome(filterUiState = RoutineHomeScaffoldFilterUiState(isLoaded = false))

        composeRule.runOnIdle { environment.filterUiStateFlow.value = appliedFilter(setOf(Arb.kotlinUuid().next())) }
        composeRule.waitForIdle()
        composeRule.runOnIdle { environment.pagingDataFlow.value = filterCaseRoutinePagingDataOf(environment.summaryList + addedSummary()) }
        composeRule.waitForIdle()

        assertPositionKept(environment)
    }

    private fun appliedFilter(tagIdSet: Set<Uuid>): RoutineHomeScaffoldFilterUiState = RoutineHomeScaffoldFilterUiState(selectedTagIdSet = tagIdSet, storedTagIdSet = tagIdSet)

    private fun addedSummary(): RoutineSummary = filterCaseRoutineSummaryOf(title = "${TITLE_PREFIX}Added")

    private fun assertPositionKept(environment: Environment) {
        composeRule.onNodeWithText(environment.titleAt(SCROLLED_INDEX)).assertIsDisplayed()
        composeRule.onNodeWithText(environment.titleAt(0)).assertDoesNotExist()
    }

    private fun setScrolledRoutineHome(filterUiState: RoutineHomeScaffoldFilterUiState = RoutineHomeScaffoldFilterUiState()): Environment {
        val summaryList = List(ROUTINE_COUNT) { index -> filterCaseRoutineSummaryOf(title = "$TITLE_PREFIX${index.toString().padStart(length = 2, padChar = '0')}") }
        val filterUiStateFlow = MutableStateFlow(filterUiState)
        val pagingDataFlow = MutableStateFlow(filterCaseRoutinePagingDataOf(summaryList))
        val viewModel = mockk<RoutineHomeViewModel>(relaxed = true)
        every { viewModel.routinePagingData } returns pagingDataFlow
        every { viewModel.filterUiState } returns filterUiStateFlow
        every { viewModel.effect } returns emptyFlow()
        every { viewModel.recordEffect } returns emptyFlow()
        val calendarViewModel = routineHomeCalendarViewModel()
        val holidayViewModel = routineHomeHolidayViewModel()
        val syncViewModel = routineSyncRefreshViewModel()

        composeRule.setContent {
            DiaryTheme {
                RoutineHomeScreen(
                    navigateToAdd = {},
                    navigateToDetail = {},
                    navigateToFilter = {},
                    navigateToFinishedList = {},
                    componentVisibleProvider = { RoutineHomeScaffoldComponentVisible() },
                    homeReselectEvent = emptyFlow(),
                    routineViewModel = viewModel,
                    calendarViewModel = calendarViewModel,
                    holidayViewModel = holidayViewModel,
                    syncViewModel = syncViewModel,
                )
            }
        }
        val environment = Environment(summaryList = summaryList, filterUiStateFlow = filterUiStateFlow, pagingDataFlow = pagingDataFlow)
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(environment.titleAt(0)).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.filterCaseRefreshableList().performScrollToIndex(SCROLLED_INDEX)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(environment.titleAt(SCROLLED_INDEX)).assertIsDisplayed()
        composeRule.onNodeWithText(environment.titleAt(0)).assertDoesNotExist()

        return environment
    }

    // 필터를 바꾼 뒤 좁힌 결과가 이어서 도착한다. 좁힌 결과에도 보던 루틴이 남아 있어 그 자리를 따라가지 않는지 확인할 수 있다.
    private fun changeQuery(
        environment: Environment,
        filterUiState: RoutineHomeScaffoldFilterUiState,
    ) {
        composeRule.runOnIdle { environment.filterUiStateFlow.value = filterUiState }
        composeRule.waitForIdle()
        composeRule.runOnIdle { environment.pagingDataFlow.value = filterCaseRoutinePagingDataOf(environment.narrowedList) }
        composeRule.waitForIdle()
    }

    private fun assertBackToTop(environment: Environment) {
        val narrowedFirstTitle =
            environment.narrowedList
                .first()
                .routine.detail.title

        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(narrowedFirstTitle).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(narrowedFirstTitle).assertIsDisplayed()
        composeRule.onNodeWithText(environment.titleAt(SCROLLED_INDEX)).assertDoesNotExist()
    }

    private class Environment(
        val summaryList: List<RoutineSummary>,
        val filterUiStateFlow: MutableStateFlow<RoutineHomeScaffoldFilterUiState>,
        val pagingDataFlow: MutableStateFlow<PagingData<RoutineSummary>>,
    ) {
        // 보던 루틴이 들어 있는 좁힌 결과다.
        val narrowedList: List<RoutineSummary> = summaryList.filterIndexed { index, _ -> index % 2 == 0 }

        fun titleAt(index: Int): String = summaryList[index].routine.detail.title
    }

    private companion object {
        const val ROUTINE_COUNT = 60
        const val SCROLLED_INDEX = 50
        const val TIMEOUT_MILLIS = 5_000L
        const val TITLE_PREFIX = "FilterScrollRoutine"
    }
}
