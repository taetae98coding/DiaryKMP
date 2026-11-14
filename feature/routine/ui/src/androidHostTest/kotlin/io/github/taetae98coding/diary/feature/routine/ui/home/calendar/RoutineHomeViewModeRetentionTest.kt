package io.github.taetae98coding.diary.feature.routine.ui.home.calendar

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTouchInput
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import io.github.taetae98coding.diary.core.model.routine.CalendarRoutineRecord
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.testing.routine.routineSummary
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshUiState
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import io.github.taetae98coding.diary.feature.routine.api.RoutineHomeNavKey
import io.github.taetae98coding.diary.feature.routine.ui.home.RoutineHomeScaffoldFilterUiState
import io.github.taetae98coding.diary.feature.routine.ui.home.RoutineHomeViewModel
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarTestFixture.JULY_15_2026
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarTestFixture.JULY_2026
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarTestFixture.KOREAN_CALENDAR_VIEW_MODE_DESCRIPTION
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarTestFixture.KOREAN_LIST_VIEW_MODE_DESCRIPTION
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarTestFixture.july
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarTestFixture.koreanTitle
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarTestFixture.record
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.holiday.RoutineHomeHolidayUiState
import io.github.taetae98coding.diary.feature.routine.ui.home.viewmode.RoutineHomeViewMode
import io.github.taetae98coding.diary.feature.routine.ui.loadedRoutinePagingData
import io.github.taetae98coding.diary.feature.routine.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.feature.routine.ui.routineHomeViewModel
import io.github.taetae98coding.diary.feature.routine.ui.routineSyncRefreshViewModel
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.every
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "ko-w411dp-h891dp")
class RoutineHomeViewModeRetentionTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val routinePagingData = MutableStateFlow(loadedRoutinePagingData())
    private val filterUiState = MutableStateFlow(RoutineHomeScaffoldFilterUiState())
    private val calendarUiState = MutableStateFlow(RoutineHomeCalendarUiState())
    private val syncUiState = MutableStateFlow(SyncRefreshUiState())
    private val navigatedDetailIdList = mutableListOf<Uuid>()
    private val calendarViewModel = RoutineHomeCalendarTestFixture.calendarViewModel(uiState = calendarUiState)
    private val holidayViewModel = RoutineHomeCalendarTestFixture.holidayViewModel(uiState = MutableStateFlow(RoutineHomeHolidayUiState()))
    private val syncViewModel = syncViewModel()
    private val defaultRoutineViewModel = routineViewModel()
    private var isRoutineHomeShown by mutableStateOf(true)

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-79b8dc32-7477-42c7-821e-909e0a7f3204 달력 보기에서 보기 전환 없이 내용이 바뀌어도 달력 보기를 유지한다`() {
        calendarUiState.value = RoutineHomeCalendarUiState(recordList = listOf(record(title = WORKOUT_TITLE, date = july(day = 10))))
        routinePagingData.value = loadedRoutinePagingData(listOf(summary(title = WORKOUT_TITLE)))
        composeRule.setContent { RoutineHomeTestScreen() }
        composeRule.waitForIdle()
        assertCalendarShown(yearMonth = JULY_2026)
        val changeByName =
            mapOf(
                "마지막 루틴이 삭제되어 표시할 기록이 없어짐" to {
                    routinePagingData.value = loadedRoutinePagingData()
                    calendarUiState.value = RoutineHomeCalendarUiState()
                },
                "사용자가 태그 필터 선택을 바꿈" to {
                    val tagIdSet = setOf(Arb.kotlinUuid().next())
                    filterUiState.value = RoutineHomeScaffoldFilterUiState(selectedTagIdSet = tagIdSet, storedTagIdSet = tagIdSet)
                },
                "새로고침이 시작됨" to { syncUiState.value = SyncRefreshUiState(isRefreshing = true) },
                "새로고침이 끝남" to { syncUiState.value = SyncRefreshUiState(isRefreshing = false) },
                "로그아웃으로 현재 사용자 계정이 바뀜" to {
                    routinePagingData.value = loadedRoutinePagingData()
                    calendarUiState.value = RoutineHomeCalendarUiState()
                    filterUiState.value = RoutineHomeScaffoldFilterUiState()
                },
            )

        changeByName.forEach { (name, change) ->
            composeRule.runOnIdle { change() }
            composeRule.waitForIdle()

            withClue(name) { assertCalendarShown(yearMonth = JULY_2026) }
        }
    }

    @Test
    fun `TC-79b8dc32-7477-42c7-821e-909e0a7f3204 목록 보기에서 기록이 있는 루틴이 새로 저장되어도 목록 보기를 유지한다`() {
        composeRule.setContent { RoutineHomeTestScreen(initialViewMode = RoutineHomeViewMode.LIST) }
        composeRule.waitForIdle()
        assertListShown()

        composeRule.runOnIdle {
            routinePagingData.value = loadedRoutinePagingData(listOf(summary(title = WORKOUT_TITLE)))
            calendarUiState.value = RoutineHomeCalendarUiState(recordList = listOf(record(title = WORKOUT_TITLE, date = july(day = 10))))
        }
        composeRule.waitForIdle()

        assertListShown()
        composeRule.onNodeWithText(WORKOUT_TITLE).assertIsDisplayed()
    }

    @Test
    fun `TC-ea461024-62df-46c0-b869-43019c5218f8 화면이 재생성되어도 바꿔 둔 달력 보기와 보던 달을 유지한다`() {
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent { RoutineHomeTestScreen(initialViewMode = RoutineHomeViewMode.LIST) }
        showSeptemberCalendar()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        assertCalendarShown(yearMonth = SEPTEMBER_2026)
    }

    @Test
    fun `TC-ea461024-62df-46c0-b869-43019c5218f8 앱이 백그라운드에 갔다가 돌아와도 바꿔 둔 달력 보기와 보던 달을 유지한다`() {
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.RESUMED)
        composeRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                RoutineHomeTestScreen(initialViewMode = RoutineHomeViewMode.LIST)
            }
        }
        showSeptemberCalendar()

        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.CREATED }
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.RESUMED }
        composeRule.waitForIdle()

        assertCalendarShown(yearMonth = SEPTEMBER_2026)
    }

    @Test
    fun `TC-ea461024-62df-46c0-b869-43019c5218f8 루틴 추가나 루틴 상세에 다녀와도 바꿔 둔 달력 보기와 보던 달을 유지한다`() {
        setReturnableRoutineHomeScreen()
        showSeptemberCalendar()

        listOf("루틴 추가로 이동했다가 뒤로가기", "달력의 루틴 기록을 선택해 루틴 상세로 이동했다가 뒤로가기").forEach { boundary ->
            leaveToOtherScreenAndReturn()

            withClue(boundary) { assertCalendarShown(yearMonth = SEPTEMBER_2026) }
        }
    }

    @Test
    fun `TC-f3b4f76c-09b8-4eec-9e58-ed82aa4e701b 시스템이 앱을 정리했다가 다시 만들어도 달력 보기와 보던 달을 그대로 본다`() {
        var routineViewModel = routineViewModel()
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent { RoutineHomeTestScreen(initialViewMode = RoutineHomeViewMode.LIST, routineViewModel = routineViewModel) }
        showSeptemberCalendar()

        // 시스템이 정리한 앱을 다시 만들면 ViewModel은 새로 만들어지고 저장해 둔 화면 상태만 복원된다.
        routineViewModel = routineViewModel()
        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        assertCalendarShown(yearMonth = SEPTEMBER_2026)
    }

    @Test
    fun `TC-0a157b74-f1da-47aa-a53d-c4008c0c6619 다른 주요 목적지에 다녀오면 목록 보기로 초기화하고 달력은 오늘이 속한 달을 보여 준다`() {
        val backStack = NavBackStack<ScreenNavKey>(RoutineHomeNavKey)
        composeRule.setContent {
            NavDisplay(
                backStack = backStack,
                entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
                entryProvider =
                    entryProvider {
                        entry<RoutineHomeNavKey> { RoutineHomeTestScreen(initialViewMode = RoutineHomeViewMode.LIST) }
                        entry<RoutineHomeViewModeTopLevelNavKey> { Text(text = TOP_LEVEL_CONTENT) }
                    },
            )
        }
        showSeptemberCalendar()

        // 공통 내비게이션은 주요 목적지를 옮길 때 떠난 목적지를 전환 이력에서 빼고 새 목적지만 남긴다.
        composeRule.runOnIdle {
            backStack.clear()
            backStack.add(RoutineHomeViewModeTopLevelNavKey)
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(TOP_LEVEL_CONTENT).assertIsDisplayed()
        composeRule.runOnIdle {
            backStack.clear()
            backStack.add(RoutineHomeNavKey)
        }
        composeRule.waitForIdle()

        assertListShown()
        composeRule.routineHomeCalendarToggleViewMode(description = KOREAN_CALENDAR_VIEW_MODE_DESCRIPTION)
        assertCalendarShown(yearMonth = JULY_2026)
    }

    @Test
    fun `TC-0a157b74-f1da-47aa-a53d-c4008c0c6619 앱을 다시 실행하면 목록 보기로 초기화하고 달력은 오늘이 속한 달을 보여 준다`() {
        var launchCount by mutableIntStateOf(0)
        // 앱을 다시 실행하면 저장해 둔 화면 상태가 없으므로, 새 키로 저장 상태 없이 화면을 다시 구성한다.
        composeRule.setContent {
            key(launchCount) { RoutineHomeTestScreen(initialViewMode = RoutineHomeViewMode.LIST) }
        }
        showSeptemberCalendar()

        composeRule.runOnIdle { launchCount += 1 }
        composeRule.waitForIdle()

        assertListShown()
        composeRule.routineHomeCalendarToggleViewMode(description = KOREAN_CALENDAR_VIEW_MODE_DESCRIPTION)
        assertCalendarShown(yearMonth = JULY_2026)
    }

    @Test
    fun `TC-af979bcf-a9cb-48b3-a4a5-00e43815a292 달력 보기에 다녀오면 목록의 맨 위부터 보여 준다`() {
        val summaryList = List(ROUTINE_COUNT) { index -> summary(title = "$ROUTINE_TITLE_PREFIX${index.toString().padStart(length = 2, padChar = '0')}") }
        routinePagingData.value = loadedRoutinePagingData(summaryList)
        composeRule.setContent { RoutineHomeTestScreen(initialViewMode = RoutineHomeViewMode.LIST) }
        composeRule.waitForIdle()
        composeRule.onNode(hasScrollToIndexAction()).performScrollToIndex(index = ROUTINE_COUNT - 1)
        composeRule.waitForIdle()
        composeRule
            .onNodeWithText(
                summaryList
                    .first()
                    .routine.detail.title,
            ).assertIsNotDisplayed()

        composeRule.routineHomeCalendarToggleViewMode(description = KOREAN_CALENDAR_VIEW_MODE_DESCRIPTION)
        composeRule.routineHomeCalendarToggleViewMode(description = KOREAN_LIST_VIEW_MODE_DESCRIPTION)

        composeRule
            .onNodeWithText(
                summaryList
                    .first()
                    .routine.detail.title,
            ).assertIsDisplayed()
    }

    @Test
    fun `TC-9b0ad3d8-df71-46d3-a7e2-463c92e0fca3 달력에서 연 루틴 상세에서 돌아오면 달력 보기와 보던 달을 그대로 보고 바뀐 기록을 표시한다`() {
        val firstDate = LocalDate(year = 2026, month = Month.SEPTEMBER, day = 10)
        val secondDate = LocalDate(year = 2026, month = Month.SEPTEMBER, day = 12)
        val routineId = Arb.kotlinUuid().next()
        val firstRecord = record(title = WORKOUT_TITLE, date = firstDate, routineId = routineId)
        val secondRecord = record(title = WORKOUT_TITLE, date = secondDate, routineId = routineId)
        calendarUiState.value = RoutineHomeCalendarUiState(recordList = listOf(firstRecord, secondRecord))
        setReturnableRoutineHomeScreen()
        showSeptemberCalendar()

        // 상세에서 뒤로가기
        returnFromDetail(itemText = WORKOUT_ONCE_TEXT, itemDate = firstDate, routineId = routineId)
        assertCalendarShown(yearMonth = SEPTEMBER_2026)
        assertItemCount(text = WORKOUT_ONCE_TEXT, date = firstDate, count = 1)
        assertItemCount(text = WORKOUT_ONCE_TEXT, date = secondDate, count = 1)

        // 상세에서 제목을 고쳐 반영한 뒤 뒤로가기
        returnFromDetail(itemText = WORKOUT_ONCE_TEXT, itemDate = firstDate, routineId = routineId) {
            listOf(firstRecord.copy(title = GYM_TITLE), secondRecord.copy(title = GYM_TITLE))
        }
        assertCalendarShown(yearMonth = SEPTEMBER_2026)
        composeRule.onAllNodesWithText(WORKOUT_ONCE_TEXT).assertCountEquals(0)
        assertItemCount(text = GYM_ONCE_TEXT, date = firstDate, count = 1)
        assertItemCount(text = GYM_ONCE_TEXT, date = secondDate, count = 1)

        // 상세에서 그 날짜의 기록을 모두 뺀 뒤 뒤로가기
        returnFromDetail(itemText = GYM_ONCE_TEXT, itemDate = firstDate, routineId = routineId) {
            listOf(secondRecord.copy(title = GYM_TITLE))
        }
        assertCalendarShown(yearMonth = SEPTEMBER_2026)
        assertItemCount(text = GYM_ONCE_TEXT, date = firstDate, count = 0)
        assertItemCount(text = GYM_ONCE_TEXT, date = secondDate, count = 1)

        // 상세에서 그 루틴을 삭제
        returnFromDetail(itemText = GYM_ONCE_TEXT, itemDate = secondDate, routineId = routineId) { emptyList() }
        assertCalendarShown(yearMonth = SEPTEMBER_2026)
        composeRule.onAllNodesWithTag(ROUTINE_HOME_CALENDAR_RECORD_TEST_TAG).assertCountEquals(0)
    }

    private fun returnFromDetail(
        itemText: String,
        itemDate: LocalDate,
        routineId: Uuid,
        changedRecordList: (() -> List<CalendarRoutineRecord>)? = null,
    ) {
        navigatedDetailIdList.clear()
        val bounds = composeRule.routineHomeCalendarItemBoundsOn(text = itemText, date = itemDate, yearMonth = SEPTEMBER_2026).single()
        composeRule.onRoot().performTouchInput { click(position = bounds.center) }
        composeRule.waitForIdle()
        navigatedDetailIdList shouldBe listOf(routineId)

        composeRule.runOnIdle { isRoutineHomeShown = false }
        composeRule.waitForIdle()
        changedRecordList?.let { recordList -> composeRule.runOnIdle { calendarUiState.value = RoutineHomeCalendarUiState(recordList = recordList()) } }
        composeRule.runOnIdle { isRoutineHomeShown = true }
        composeRule.waitForIdle()
    }

    private fun assertItemCount(
        text: String,
        date: LocalDate,
        count: Int,
    ) {
        withClue("$text on $date") {
            composeRule.routineHomeCalendarItemBoundsOn(text = text, date = date, yearMonth = SEPTEMBER_2026) shouldHaveSize count
        }
    }

    private fun showSeptemberCalendar() {
        composeRule.waitForIdle()
        composeRule.routineHomeCalendarToggleViewMode(description = KOREAN_CALENDAR_VIEW_MODE_DESCRIPTION)
        repeat(SEPTEMBER_2026.month.ordinal - JULY_2026.month.ordinal) { composeRule.routineHomeCalendarMoveToNextMonth() }
        assertCalendarShown(yearMonth = SEPTEMBER_2026)
    }

    private fun leaveToOtherScreenAndReturn() {
        composeRule.runOnIdle { isRoutineHomeShown = false }
        composeRule.waitForIdle()
        composeRule.runOnIdle { isRoutineHomeShown = true }
        composeRule.waitForIdle()
    }

    private fun assertCalendarShown(yearMonth: YearMonth) {
        composeRule.onNodeWithContentDescription(KOREAN_LIST_VIEW_MODE_DESCRIPTION).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(KOREAN_CALENDAR_VIEW_MODE_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithText(koreanTitle(yearMonth)).assertIsDisplayed()
        composeRule.routineHomeCalendarDateTexts() shouldHaveSize RoutineHomeCalendarTestFixture.DAYS_PER_CALENDAR
    }

    private fun assertListShown() {
        composeRule.onNodeWithContentDescription(KOREAN_CALENDAR_VIEW_MODE_DESCRIPTION).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(KOREAN_LIST_VIEW_MODE_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithText(ROUTINE_HOME_TITLE).assertIsDisplayed()
        composeRule.routineHomeCalendarDateTexts() shouldHaveSize 0
    }

    private fun setReturnableRoutineHomeScreen() {
        composeRule.setContent {
            // 내비게이션이 뒤에 쌓인 화면을 컴포지션에서 내리고 저장 상태만 보관하는 것을 그대로 따른다.
            val saveableStateHolder = rememberSaveableStateHolder()
            if (isRoutineHomeShown) {
                saveableStateHolder.SaveableStateProvider(key = ROUTINE_HOME_ENTRY_KEY) {
                    RoutineHomeTestScreen(initialViewMode = RoutineHomeViewMode.LIST)
                }
            }
        }
        composeRule.waitForIdle()
    }

    @Composable
    private fun RoutineHomeTestScreen(
        initialViewMode: RoutineHomeViewMode = RoutineHomeViewMode.CALENDAR,
        routineViewModel: RoutineHomeViewModel = defaultRoutineViewModel,
    ) {
        RoutineHomeCalendarTestScreen(
            today = JULY_15_2026,
            calendarViewModel = calendarViewModel,
            holidayViewModel = holidayViewModel,
            initialViewMode = initialViewMode,
            routineViewModel = routineViewModel,
            syncViewModel = syncViewModel,
            navigateToDetail = navigatedDetailIdList::add,
        )
    }

    private fun routineViewModel(): RoutineHomeViewModel =
        routineHomeViewModel().also { viewModel ->
            every { viewModel.routinePagingData } returns routinePagingData
            every { viewModel.filterUiState } returns filterUiState
        }

    private fun syncViewModel(): SyncRefreshViewModel =
        routineSyncRefreshViewModel().also { viewModel ->
            every { viewModel.uiState } returns syncUiState
        }

    private fun summary(title: String): RoutineSummary {
        val summary = Arb.routineSummary().next()

        return summary.copy(
            routine =
                summary.routine.copy(
                    detail = summary.routine.detail.copy(title = title, startDate = july(day = 1)),
                    isDeleted = false,
                ),
        )
    }

    private companion object {
        const val ROUTINE_HOME_ENTRY_KEY = "RoutineHome"
        const val ROUTINE_HOME_TITLE = "루틴"
        const val TOP_LEVEL_CONTENT = "TopLevelContent"
        const val WORKOUT_TITLE = "운동"
        const val WORKOUT_ONCE_TEXT = "운동 1회"
        const val GYM_TITLE = "헬스"
        const val GYM_ONCE_TEXT = "헬스 1회"
        const val ROUTINE_TITLE_PREFIX = "루틴 제목 "
        const val ROUTINE_COUNT = 30
        val SEPTEMBER_2026 = YearMonth(year = 2026, month = Month.SEPTEMBER)
    }
}

// 메모나 태그처럼 루틴이 아닌 주요 목적지를 대신한다. 다른 기능의 화면 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object RoutineHomeViewModeTopLevelNavKey : ScreenNavKey {
    override val screenName: String
        get() = "RoutineHomeViewModeTopLevel"
}
