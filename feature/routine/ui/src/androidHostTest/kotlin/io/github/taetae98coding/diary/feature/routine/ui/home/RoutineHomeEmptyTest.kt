package io.github.taetae98coding.diary.feature.routine.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.empty.DIARY_EMPTY_BOX_TEST_TAG
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.routine.ROUTINE_CARD_TEST_TAG
import io.github.taetae98coding.diary.core.model.routine.RoutineDetail
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.core.model.routine.RoutineViewScope
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.routine.usecase.GetRoutineFilterTagIdUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.GetRoutineFilterUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.PageViewScopeRoutineUseCase
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.ROUTINE_HOME_CALENDAR_RECORD_TEST_TAG
import io.github.taetae98coding.diary.feature.routine.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.feature.routine.ui.routineHomeCalendarViewModel
import io.github.taetae98coding.diary.feature.routine.ui.routineHomeHolidayViewModel
import io.github.taetae98coding.diary.feature.routine.ui.routineSyncRefreshViewModel
import io.github.taetae98coding.diary.feature.routine.ui.routineViewScopeUseCase
import io.github.taetae98coding.diary.feature.routine.ui.unfinishedRoutineExistsUseCase
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class RoutineHomeEmptyTest {
    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var state: RoutineHomeScaffoldState

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-21eb35ec-b5c7-43cb-9b23-5698ed1365f2 필터를 적용한 채로 표시할 루틴이 없으면 조건에 맞는 루틴이 없음을 알린다`() {
        setRoutineHomeScaffold(pagingData = filterCaseRoutinePagingDataOf(emptyList()), filterUiState = appliedFilter())

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithText(KOREAN_FILTERED_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(KOREAN_FILTERED_EMPTY_DESCRIPTION).assertExists()
        composeRule.onNodeWithText(KOREAN_EMPTY_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-21eb35ec-b5c7-43cb-9b23-5698ed1365f2 기본 환경에서 조건에 맞는 루틴이 없음을 알린다`() {
        setRoutineHomeScaffold(pagingData = filterCaseRoutinePagingDataOf(emptyList()), filterUiState = appliedFilter())

        composeRule.onNodeWithText(DEFAULT_FILTERED_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(DEFAULT_FILTERED_EMPTY_DESCRIPTION).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-24ebfe5d-b3a9-48c4-9f6e-c1d6b3d152d9 완료되지 않은 루틴은 있지만 오늘 할 루틴이 없으면 오늘 할 루틴이 없음을 알린다`() {
        setRoutineHomeScaffold(pagingData = filterCaseRoutinePagingDataOf(emptyList()), filterUiState = todayEmptyFilter())

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithText(KOREAN_TODAY_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(KOREAN_TODAY_EMPTY_DESCRIPTION).assertExists()
        composeRule.onNodeWithText(KOREAN_EMPTY_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(KOREAN_FILTERED_EMPTY_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-24ebfe5d-b3a9-48c4-9f6e-c1d6b3d152d9 기본 환경에서 오늘 할 루틴이 없음을 알린다`() {
        setRoutineHomeScaffold(pagingData = filterCaseRoutinePagingDataOf(emptyList()), filterUiState = todayEmptyFilter())

        composeRule.onNodeWithText(DEFAULT_TODAY_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(DEFAULT_TODAY_EMPTY_DESCRIPTION).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_FILTERED_EMPTY_TITLE).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-7bd552ae-99c3-4c5e-9d48-41a4130a65ab 빈 상태에서는 가른 이유의 안내 하나만 표시한다`() {
        val tagId = Arb.kotlinUuid().next()
        var filterUiState by mutableStateOf(RoutineHomeScaffoldFilterUiState())
        val pagingDataFlow = MutableStateFlow(filterCaseRoutinePagingDataOf(emptyList()))
        composeRule.setContent {
            DiaryTheme {
                RoutineHomeScaffold(
                    onEvent = {},
                    onRoutineListEvent = {},
                    routinePagingItems = pagingDataFlow.collectAsLazyPagingItems(),
                    filterUiStateProvider = { filterUiState },
                )
            }
        }

        listOf(
            RoutineHomeScaffoldFilterUiState(viewScope = RoutineViewScope.TODAY, hasUnfinishedRoutine = false) to KOREAN_EMPTY_TITLE,
            RoutineHomeScaffoldFilterUiState(viewScope = RoutineViewScope.ALL, hasUnfinishedRoutine = false) to KOREAN_EMPTY_TITLE,
            RoutineHomeScaffoldFilterUiState(viewScope = RoutineViewScope.TODAY, hasUnfinishedRoutine = true) to KOREAN_TODAY_EMPTY_TITLE,
            RoutineHomeScaffoldFilterUiState(storedTagIdSet = setOf(tagId), viewScope = RoutineViewScope.TODAY, hasUnfinishedRoutine = true) to KOREAN_TODAY_EMPTY_TITLE,
            RoutineHomeScaffoldFilterUiState(selectedTagIdSet = setOf(tagId), storedTagIdSet = setOf(tagId), viewScope = RoutineViewScope.TODAY, hasUnfinishedRoutine = true) to KOREAN_FILTERED_EMPTY_TITLE,
            RoutineHomeScaffoldFilterUiState(selectedTagIdSet = setOf(tagId), storedTagIdSet = setOf(tagId), viewScope = RoutineViewScope.ALL, hasUnfinishedRoutine = true) to KOREAN_FILTERED_EMPTY_TITLE,
            RoutineHomeScaffoldFilterUiState(selectedTagIdSet = setOf(tagId), storedTagIdSet = setOf(tagId), viewScope = RoutineViewScope.TODAY, hasUnfinishedRoutine = false) to KOREAN_FILTERED_EMPTY_TITLE,
        ).forEach { (state, expectedTitle) ->
            composeRule.runOnIdle { filterUiState = state }
            composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) { composeRule.onAllNodesWithText(expectedTitle).fetchSemanticsNodes().isNotEmpty() }
            composeRule.mainClock.advanceTimeBy(CROSSFADE_MILLIS)
            composeRule.waitForIdle()

            listOf(KOREAN_EMPTY_TITLE, KOREAN_TODAY_EMPTY_TITLE, KOREAN_FILTERED_EMPTY_TITLE).filter { title ->
                composeRule.onAllNodesWithText(title).fetchSemanticsNodes().isNotEmpty()
            } shouldBe listOf(expectedTitle)
        }
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-7ba08dd3-fee8-4da0-bdb5-511ea813b1e7 오늘 할 루틴이 없는 빈 상태에서 모든 루틴으로 바꾸면 목록이 나타나고 되돌리면 다시 빈 상태가 된다`() {
        val notStarted = filterCaseRoutineSummaryOf(title = ROUTINE_TITLE) { detail -> detail.copy(startDate = NOT_STARTED_START_DATE) }
        var filterUiState by mutableStateOf(todayEmptyFilter())
        val pagingDataFlow = MutableStateFlow(filterCaseRoutinePagingDataOf(emptyList()))
        composeRule.setContent {
            DiaryTheme {
                RoutineHomeScaffold(
                    onEvent = {},
                    onRoutineListEvent = {},
                    routinePagingItems = pagingDataFlow.collectAsLazyPagingItems(),
                    filterUiStateProvider = { filterUiState },
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(KOREAN_TODAY_EMPTY_TITLE).assertExists()

        // 보기 범위가 저장되면 필터 상태와 그 범위로 조회한 목록이 이어서 전달된다.
        composeRule.runOnIdle {
            filterUiState = filterUiState.copy(viewScope = RoutineViewScope.ALL)
            pagingDataFlow.value = filterCaseRoutinePagingDataOf(listOf(notStarted))
        }
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) { composeRule.onAllNodes(filterCaseRoutineCard(ROUTINE_TITLE)).fetchSemanticsNodes().isNotEmpty() }
        composeRule.onNode(filterCaseRoutineCard(ROUTINE_TITLE) and hasText(KOREAN_NOT_STARTED_TEXT)).assertExists()
        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()

        composeRule.runOnIdle {
            filterUiState = filterUiState.copy(viewScope = RoutineViewScope.TODAY)
            pagingDataFlow.value = filterCaseRoutinePagingDataOf(emptyList())
        }
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) { composeRule.onAllNodesWithText(KOREAN_TODAY_EMPTY_TITLE).fetchSemanticsNodes().isNotEmpty() }
        composeRule.onNode(filterCaseRoutineCard(ROUTINE_TITLE)).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-68d686f2-29c1-4d2d-aefe-55fd3e84df8f 완료되지 않은 루틴의 유무가 달라지면 조작 없이 빈 상태 안내가 바뀐다`() {
        var filterUiState by mutableStateOf(RoutineHomeScaffoldFilterUiState(viewScope = RoutineViewScope.TODAY, hasUnfinishedRoutine = false))
        val pagingDataFlow = MutableStateFlow(filterCaseRoutinePagingDataOf(emptyList()))
        composeRule.setContent {
            DiaryTheme {
                RoutineHomeScaffold(
                    onEvent = {},
                    onRoutineListEvent = {},
                    routinePagingItems = pagingDataFlow.collectAsLazyPagingItems(),
                    filterUiStateProvider = { filterUiState },
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(KOREAN_EMPTY_TITLE).assertExists()

        composeRule.runOnIdle { filterUiState = filterUiState.copy(hasUnfinishedRoutine = true) }
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) { composeRule.onAllNodesWithText(KOREAN_TODAY_EMPTY_TITLE).fetchSemanticsNodes().isNotEmpty() }

        composeRule.runOnIdle { filterUiState = filterUiState.copy(hasUnfinishedRoutine = false) }
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) { composeRule.onAllNodesWithText(KOREAN_EMPTY_TITLE).fetchSemanticsNodes().isNotEmpty() }
        composeRule.mainClock.advanceTimeBy(CROSSFADE_MILLIS)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(KOREAN_TODAY_EMPTY_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-5af87806-0c0c-4dd1-b948-0fd3eb19e8d3 목록을 불러오는 동안에는 빈 상태 안내를 표시하지 않는다`() {
        setRoutineHomeScaffold(pagingData = filterCaseLoadingRoutinePagingData())

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_FILTERED_EMPTY_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-ff40f9dc-74c9-45e2-8314-922c9564d423 오늘 할 루틴에서 진행 중인 루틴이 있으면 빈 상태 안내를 표시하지 않는다`() {
        assertRoutineShownWithoutEmpty(viewScope = RoutineViewScope.TODAY) { detail -> detail }
    }

    @Test
    fun `TC-ff40f9dc-74c9-45e2-8314-922c9564d423 모든 루틴에서 진행 중인 루틴이 있으면 빈 상태 안내를 표시하지 않는다`() {
        assertRoutineShownWithoutEmpty(viewScope = RoutineViewScope.ALL) { detail -> detail }
    }

    @Test
    fun `TC-ff40f9dc-74c9-45e2-8314-922c9564d423 모든 루틴에서 시작 전인 루틴이 있으면 빈 상태 안내를 표시하지 않는다`() {
        assertRoutineShownWithoutEmpty(viewScope = RoutineViewScope.ALL) { detail -> detail.copy(startDate = NOT_STARTED_START_DATE) }
    }

    @Test
    fun `TC-2e9b4548-afee-4175-8ce8-99eefb1838e8 루틴이 하나도 없어도 달력 보기에는 빈 상태 안내가 없다`() {
        assertCalendarWithoutEmpty(pagingData = filterCaseRoutinePagingDataOf(emptyList()))
    }

    @Test
    fun `TC-2e9b4548-afee-4175-8ce8-99eefb1838e8 루틴에 기록이 하나도 없어도 달력 보기에는 빈 상태 안내가 없다`() {
        assertCalendarWithoutEmpty(pagingData = filterCaseRoutinePagingDataOf(listOf(filterCaseRoutineSummaryOf(title = ROUTINE_TITLE))))
    }

    @Test
    fun `TC-2e9b4548-afee-4175-8ce8-99eefb1838e8 선택한 태그와 연결된 루틴이 없어도 달력 보기에는 빈 상태 안내가 없다`() {
        assertCalendarWithoutEmpty(pagingData = filterCaseRoutinePagingDataOf(emptyList()), filterUiState = appliedFilter())
    }

    @Test
    fun `TC-02029f5d-0bca-4746-92aa-8fb254bf6768 처음 불러오기에 실패하면 오류 안내 없이 빈 상태 안내를 표시한다`() {
        setRoutineHomeScaffold(pagingData = filterCaseFailedRoutinePagingData())

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithTag(ROUTINE_CARD_TEST_TAG).assertDoesNotExist()
        state.snackbarHostState.currentSnackbarData shouldBe null
    }

    // 조회에 실패한 결과를 ViewModel이 화면에 넘기는 것까지 함께 확인한다.
    @Test
    fun `TC-02029f5d-0bca-4746-92aa-8fb254bf6768 목록을 조회하지 못하면 오류 안내 없이 빈 상태로 표시한다`() {
        val pageRoutineUseCase = mockk<PageViewScopeRoutineUseCase>()
        every { pageRoutineUseCase(parameter = any()) } returns flowOf(Result.failure(IllegalStateException(Arb.string().next())))
        val getRoutineFilterUseCase = mockk<GetRoutineFilterUseCase>()
        every { getRoutineFilterUseCase() } returns flowOf(Result.success(emptyList()))
        val getRoutineFilterTagIdUseCase = mockk<GetRoutineFilterTagIdUseCase>()
        every { getRoutineFilterTagIdUseCase() } returns flowOf(Result.success(emptySet()))
        val viewModel =
            RoutineHomeViewModel(
                getRoutineFilterUseCase = getRoutineFilterUseCase,
                getRoutineFilterTagIdUseCase = getRoutineFilterTagIdUseCase,
                getRoutineViewScopeUseCase = routineViewScopeUseCase(),
                getUnfinishedRoutineExistsUseCase = unfinishedRoutineExistsUseCase(),
                pageViewScopeRoutineUseCase = pageRoutineUseCase,
                finishRoutineUseCase = mockk(),
                restartRoutineUseCase = mockk(),
                deleteRoutineUseCase = mockk(),
                restoreRoutineUseCase = mockk(),
                addQuickRoutineRecordUseCase = mockk(),
                deleteRoutineRecordUseCase = mockk(),
            )
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
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(DEFAULT_EMPTY_TITLE).fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithTag(ROUTINE_CARD_TEST_TAG).assertDoesNotExist()
        composeRule.onNode(isDialog()).assertDoesNotExist()
        // 화면에 보이는 글은 상단 바 제목, 완료된 루틴 진입 라벨과 빈 상태 안내뿐이다.
        composeRule
            .onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Text), useUnmergedTree = true)
            .fetchSemanticsNodes()
            .flatMap { node -> node.config[SemanticsProperties.Text].map { text -> text.text } }
            .filter { text -> text.isNotEmpty() } shouldContainExactlyInAnyOrder listOf(DEFAULT_TITLE, DEFAULT_FINISHED_LIST_LABEL, DEFAULT_EMPTY_TITLE, DEFAULT_EMPTY_DESCRIPTION)
    }

    private fun appliedFilter(): RoutineHomeScaffoldFilterUiState {
        val tagId = Arb.kotlinUuid().next()

        return RoutineHomeScaffoldFilterUiState(selectedTagIdSet = setOf(tagId), storedTagIdSet = setOf(tagId))
    }

    private fun todayEmptyFilter(): RoutineHomeScaffoldFilterUiState = RoutineHomeScaffoldFilterUiState(viewScope = RoutineViewScope.TODAY, hasUnfinishedRoutine = true)

    private fun assertRoutineShownWithoutEmpty(
        viewScope: RoutineViewScope,
        transform: (RoutineDetail) -> RoutineDetail,
    ) {
        setRoutineHomeScaffold(
            pagingData = filterCaseRoutinePagingDataOf(listOf(filterCaseRoutineSummaryOf(title = ROUTINE_TITLE, transform = transform))),
            filterUiState = appliedFilter().copy(viewScope = viewScope, hasUnfinishedRoutine = true),
        )

        composeRule.onNode(filterCaseRoutineCard(ROUTINE_TITLE)).assertExists()
        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
    }

    // 달력에 표시할 기록이 없는 상태로 달력 보기로 바꾼다.
    private fun assertCalendarWithoutEmpty(
        pagingData: PagingData<RoutineSummary>,
        filterUiState: RoutineHomeScaffoldFilterUiState = RoutineHomeScaffoldFilterUiState(),
    ) {
        setRoutineHomeScaffold(pagingData = pagingData, filterUiState = filterUiState)

        composeRule.onNodeWithContentDescription(DEFAULT_CALENDAR_VIEW_MODE_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_TODAY_DESCRIPTION).assertExists()
        composeRule.onNodeWithTag(ROUTINE_HOME_CALENDAR_RECORD_TEST_TAG).assertDoesNotExist()
        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_FILTERED_EMPTY_TITLE).assertDoesNotExist()
        state.snackbarHostState.currentSnackbarData shouldBe null
    }

    private fun setRoutineHomeScaffold(
        pagingData: PagingData<RoutineSummary>,
        filterUiState: RoutineHomeScaffoldFilterUiState = RoutineHomeScaffoldFilterUiState(),
    ) {
        val pagingDataFlow = MutableStateFlow(pagingData)

        composeRule.setContent {
            DiaryTheme {
                state = rememberRoutineHomeScaffoldState()

                RoutineHomeScaffold(
                    onEvent = {},
                    onRoutineListEvent = {},
                    state = state,
                    routinePagingItems = pagingDataFlow.collectAsLazyPagingItems(),
                    filterUiStateProvider = { filterUiState },
                )
            }
        }
        composeRule.waitForIdle()
    }

    private companion object {
        val NOT_STARTED_START_DATE: LocalDate = LocalDate(year = 2999, month = 1, day = 1)
        const val KOREAN_EMPTY_TITLE = "아직 루틴이 없습니다"
        const val KOREAN_FILTERED_EMPTY_TITLE = "조건에 맞는 루틴이 없습니다"
        const val KOREAN_FILTERED_EMPTY_DESCRIPTION = "필터를 바꾸면 다른 루틴을 볼 수 있습니다"
        const val KOREAN_TODAY_EMPTY_TITLE = "오늘 할 루틴이 없습니다"
        const val KOREAN_TODAY_EMPTY_DESCRIPTION = "필터에서 모든 루틴을 볼 수 있습니다"
        const val KOREAN_NOT_STARTED_TEXT = "시작 전"
        const val DEFAULT_TODAY_EMPTY_TITLE = "No routines for today"
        const val DEFAULT_TODAY_EMPTY_DESCRIPTION = "Use the filter to see all routines."
        const val CROSSFADE_MILLIS = 1_000L
        const val DEFAULT_TITLE = "Routine"
        const val DEFAULT_FINISHED_LIST_LABEL = "Finished routines"
        const val DEFAULT_EMPTY_TITLE = "No routines yet"
        const val DEFAULT_EMPTY_DESCRIPTION = "Use the add button to create a routine."
        const val DEFAULT_FILTERED_EMPTY_TITLE = "No routines match the filter"
        const val DEFAULT_FILTERED_EMPTY_DESCRIPTION = "Change the filter to see other routines."
        const val DEFAULT_CALENDAR_VIEW_MODE_DESCRIPTION = "Show calendar"
        const val DEFAULT_TODAY_DESCRIPTION = "Go to today"
        const val ROUTINE_TITLE = "Water"
        const val TIMEOUT_MILLIS = 5_000L
    }
}
