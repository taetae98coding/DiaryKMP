package io.github.taetae98coding.diary.feature.routine.ui.home.calendar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.text.TextLayoutResult
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.calendar.CalendarDefaults
import io.github.taetae98coding.diary.compose.calendar.rememberCalendarState
import io.github.taetae98coding.diary.compose.core.snackbar.DismissUndoSnackbarEffect
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.routine.RoutineListEvent
import io.github.taetae98coding.diary.compose.routine.RoutineListState
import io.github.taetae98coding.diary.compose.routine.UpdateRoutineListTodayEffect
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.core.model.routine.CalendarRoutineRecord
import io.github.taetae98coding.diary.core.testing.routine.calendarRoutineRecord
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import io.github.taetae98coding.diary.feature.routine.ui.home.FetchCalendarRecordEffect
import io.github.taetae98coding.diary.feature.routine.ui.home.FetchHolidayEffect
import io.github.taetae98coding.diary.feature.routine.ui.home.RoutineHomeScaffold
import io.github.taetae98coding.diary.feature.routine.ui.home.RoutineHomeScaffoldEvent
import io.github.taetae98coding.diary.feature.routine.ui.home.RoutineHomeScaffoldState
import io.github.taetae98coding.diary.feature.routine.ui.home.RoutineHomeViewModel
import io.github.taetae98coding.diary.feature.routine.ui.home.ScrollToStartOnReselectEffect
import io.github.taetae98coding.diary.feature.routine.ui.home.UpdateRoutineHomeTodayEffect
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.holiday.RoutineHomeHolidayUiState
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.holiday.RoutineHomeHolidayViewModel
import io.github.taetae98coding.diary.feature.routine.ui.home.rememberRoutineHomeScaffoldState
import io.github.taetae98coding.diary.feature.routine.ui.home.viewmode.RoutineHomeViewMode
import io.github.taetae98coding.diary.feature.routine.ui.home.viewmode.rememberRoutineHomeViewModeState
import io.github.taetae98coding.diary.feature.routine.ui.routineHomeViewModel
import io.github.taetae98coding.diary.feature.routine.ui.routineSyncRefreshViewModel
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.daysUntil
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.toInstant
import kotlinx.datetime.yearMonth
import kotlin.math.abs
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal object RoutineHomeCalendarTestFixture {
    const val KOREAN_CALENDAR_VIEW_MODE_DESCRIPTION = "달력으로 보기"
    const val KOREAN_LIST_VIEW_MODE_DESCRIPTION = "목록으로 보기"
    const val KOREAN_TODAY_BUTTON_DESCRIPTION = "오늘로 이동"
    const val KOREAN_REFRESHING_DESCRIPTION = "새로고침 중"
    const val DEFAULT_CALENDAR_VIEW_MODE_DESCRIPTION = "Show calendar"
    const val DEFAULT_LIST_VIEW_MODE_DESCRIPTION = "Show list"
    const val DEFAULT_TODAY_BUTTON_DESCRIPTION = "Go to today"
    const val DEFAULT_REFRESHING_DESCRIPTION = "Refreshing"
    const val DEFAULT_NEXT_MONTH_DESCRIPTION = "Change to next month"
    const val DEFAULT_CONFIRM = "OK"
    const val DEFAULT_CANCEL = "Cancel"
    const val DAYS_PER_CALENDAR = 42
    const val DAYS_PER_WEEK = 7

    val JULY_15_2026: LocalDate = LocalDate(year = 2026, month = Month.JULY, day = 15)
    val JULY_2026: YearMonth = YearMonth(year = 2026, month = Month.JULY)

    fun koreanTitle(yearMonth: YearMonth): String = "${yearMonth.year}년 ${yearMonth.month.number}월"

    fun englishTitle(yearMonth: YearMonth): String = "${englishMonthTitle(yearMonth)} ${yearMonth.year}"

    fun englishDayCellText(
        yearMonth: YearMonth,
        day: Int,
    ): String = "${englishMonthTitle(yearMonth)} $day, ${yearMonth.year}"

    fun july(day: Int): LocalDate = LocalDate(year = 2026, month = Month.JULY, day = day)

    fun noonOf(date: LocalDate): Instant = LocalDateTime(date = date, time = LocalTime(hour = 12, minute = 0)).toInstant(TimeZone.currentSystemDefault())

    fun clockAt(date: LocalDate): Clock = mockk<Clock>().also { clock -> every { clock.now() } returns noonOf(date) }

    fun record(
        title: String,
        date: LocalDate,
        amount: Long = 1,
        unit: String = "회",
        routineId: Uuid? = null,
    ): CalendarRoutineRecord {
        val record = Arb.calendarRoutineRecord().next()

        return record.copy(
            routineId = routineId ?: record.routineId,
            title = title,
            date = date,
            amount = amount,
            unit = unit,
        )
    }

    fun holiday(
        name: String,
        date: LocalDate,
        isHoliday: Boolean = true,
    ): Holiday =
        Holiday(
            name = name,
            isHoliday = isHoliday,
            dateRange = date..date,
        )

    fun calendarViewModel(uiState: StateFlow<RoutineHomeCalendarUiState>): RoutineHomeCalendarViewModel =
        mockk<RoutineHomeCalendarViewModel>(relaxed = true).also { viewModel ->
            every { viewModel.uiState } returns uiState
        }

    fun holidayViewModel(uiState: StateFlow<RoutineHomeHolidayUiState>): RoutineHomeHolidayViewModel =
        mockk<RoutineHomeHolidayViewModel>(relaxed = true).also { viewModel ->
            every { viewModel.uiState } returns uiState
        }

    private fun englishMonthTitle(yearMonth: YearMonth): String =
        yearMonth.month.name
            .lowercase()
            .replaceFirstChar(Char::titlecase)
}

/**
 * RoutineHomeScreen과 같은 효과와 이벤트 연결로 화면을 구성하되, 오늘과 처음 보는 달, 보기를 테스트가 정한다.
 * RoutineHomeScreen은 오늘과 처음 보는 달을 기기의 날짜에서 얻어 테스트가 정할 수 없다.
 */
@Composable
internal fun RoutineHomeCalendarTestScreen(
    today: LocalDate,
    calendarViewModel: RoutineHomeCalendarViewModel,
    holidayViewModel: RoutineHomeHolidayViewModel,
    initialYearMonth: YearMonth = today.yearMonth,
    initialViewMode: RoutineHomeViewMode = RoutineHomeViewMode.CALENDAR,
    clock: Clock = remember(today) { RoutineHomeCalendarTestFixture.clockAt(date = today) },
    routineViewModel: RoutineHomeViewModel = remember { routineHomeViewModel() },
    syncViewModel: SyncRefreshViewModel = remember { routineSyncRefreshViewModel() },
    homeReselectEvent: Flow<Unit> = emptyFlow(),
    navigateToAdd: () -> Unit = {},
    navigateToDetail: (Uuid) -> Unit = {},
    navigateToFilter: () -> Unit = {},
    onState: (RoutineHomeScaffoldState) -> Unit = {},
) {
    val state =
        rememberRoutineHomeScaffoldState(
            viewModeState = rememberRoutineHomeViewModeState(initialViewMode = initialViewMode),
            listState = remember(clock) { RoutineListState(initialToday = today, clock = clock) },
            calendarState = rememberCalendarState(initialYearMonth = initialYearMonth),
        )
    val routinePagingItems = routineViewModel.routinePagingData.collectAsLazyPagingItems()
    val filterUiState by routineViewModel.filterUiState.collectAsStateWithLifecycle()
    val calendarUiState by calendarViewModel.uiState.collectAsStateWithLifecycle()
    val holidayUiState by holidayViewModel.uiState.collectAsStateWithLifecycle()
    val syncUiState by syncViewModel.uiState.collectAsStateWithLifecycle()

    onState(state)

    DiaryTheme {
        RoutineHomeCalendarTestEffect(
            state = state,
            routineViewModel = routineViewModel,
            calendarViewModel = calendarViewModel,
            holidayViewModel = holidayViewModel,
            homeReselectEvent = homeReselectEvent,
        )

        RoutineHomeScaffold(
            onEvent = { event ->
                when (event) {
                    is RoutineHomeScaffoldEvent.ClickAdd -> navigateToAdd()

                    is RoutineHomeScaffoldEvent.ClickFilter -> navigateToFilter()

                    is RoutineHomeScaffoldEvent.ClickFinishedList -> Unit

                    is RoutineHomeScaffoldEvent.Refresh -> {
                        syncViewModel.refresh()
                        if (state.viewMode == RoutineHomeViewMode.CALENDAR) {
                            holidayViewModel.fetch(yearMonth = state.calendarState.currentYearMonth)
                        }
                    }

                    is RoutineHomeScaffoldEvent.QuickRecord -> routineViewModel.quickRecord(record = event.record, today = state.today)

                    is RoutineHomeScaffoldEvent.AmountRecord -> routineViewModel.amountRecord(record = event.record, today = state.today)

                    is RoutineHomeScaffoldEvent.ClickCalendarRecord -> navigateToDetail(event.routineId)
                }
            },
            onRoutineListEvent = { event ->
                when (event) {
                    is RoutineListEvent.ClickRoutine -> navigateToDetail(event.id)
                    is RoutineListEvent.SwipeFinish -> routineViewModel.finish(id = event.id)
                    is RoutineListEvent.SwipeRestart -> routineViewModel.restart(id = event.id)
                    is RoutineListEvent.SwipeDelete -> routineViewModel.delete(id = event.id)
                }
            },
            state = state,
            routinePagingItems = routinePagingItems,
            filterUiStateProvider = { filterUiState },
            calendarUiStateProvider = { calendarUiState },
            holidayUiStateProvider = { holidayUiState },
            syncUiStateProvider = { syncUiState },
        )
    }
}

/** 달력의 공휴일 강조 색상을 테마 안에서 얻어 둔다. */
@Composable
internal fun RoutineHomeCalendarColorCapture(onColor: (holidayColor: Color, primaryDateColor: Color) -> Unit) {
    DiaryTheme {
        onColor(CalendarDefaults.colors().sundayAndHolidayColor, DiaryTheme.colorScheme.onPrimary)
    }
}

internal fun ComposeTestRule.routineHomeCalendarMoveToNextMonth() {
    waitForIdle()
    onRoot().performTouchInput { swipeLeft() }
    waitForIdle()
}

internal fun ComposeTestRule.routineHomeCalendarMoveToPreviousMonth() {
    waitForIdle()
    onRoot().performTouchInput { swipeRight() }
    waitForIdle()
}

internal fun ComposeTestRule.routineHomeCalendarToggleViewMode(description: String) {
    onNodeWithContentDescription(description).performClick()
    waitForIdle()
}

// 상단 바의 오늘 버튼도 일 숫자를 표시하므로 빼고, 달력에 놓인 날짜 숫자만 화면 순서대로 모은다.
internal fun ComposeTestRule.routineHomeCalendarDateNodes(): List<SemanticsNode> =
    onAllNodes(hasText(text = "", substring = true))
        .fetchSemanticsNodes()
        .filterNot { node ->
            node.config
                .getOrNull(SemanticsProperties.ContentDescription)
                .orEmpty()
                .any { description ->
                    description == RoutineHomeCalendarTestFixture.KOREAN_TODAY_BUTTON_DESCRIPTION ||
                        description == RoutineHomeCalendarTestFixture.DEFAULT_TODAY_BUTTON_DESCRIPTION
                }
        }.filter { node -> node.routineHomeCalendarText().let { text -> text.isNotEmpty() && text.all(Char::isDigit) } }

internal fun ComposeTestRule.routineHomeCalendarDateTexts(): List<String> = routineHomeCalendarDateNodes().map { node -> node.routineHomeCalendarText() }

/** [yearMonth]의 달력에서 [date] 칸에 놓인 [text] 아이템의 영역. 그 칸에 없으면 빈 목록이다. */
internal fun ComposeTestRule.routineHomeCalendarItemBoundsOn(
    text: String,
    date: LocalDate,
    yearMonth: YearMonth,
): List<Rect> {
    val dateNodes = routineHomeCalendarDateNodes()
    val firstDay = yearMonth.firstDay
    val cellIndex = firstDay.minus(firstDay.dayOfWeek.isoDayNumber % RoutineHomeCalendarTestFixture.DAYS_PER_WEEK, DateTimeUnit.DAY).daysUntil(date)
    val cellBounds = dateNodes[cellIndex].boundsInRoot
    val nextWeekTop = dateNodes.getOrNull(cellIndex + RoutineHomeCalendarTestFixture.DAYS_PER_WEEK)?.boundsInRoot?.top
    val columnWidth = onRoot().fetchSemanticsNode().boundsInRoot.width / RoutineHomeCalendarTestFixture.DAYS_PER_WEEK

    return onAllNodesWithText(text)
        .fetchSemanticsNodes()
        .map { node -> node.boundsInRoot }
        .filter { bounds ->
            abs(bounds.center.x - cellBounds.center.x) < columnWidth / 2 &&
                bounds.top >= cellBounds.top &&
                (nextWeekTop == null || bounds.top < nextWeekTop)
        }
}

// 같은 숫자를 표시하는 날짜 중 주어진 색상으로 그려진 날짜의 수.
internal fun ComposeTestRule.routineHomeCalendarColoredDateCount(
    day: Int,
    color: Color,
): Int {
    val nodes = onAllNodesWithText(day.toString(), useUnmergedTree = true)

    return List(nodes.fetchSemanticsNodes().size) { index -> nodes[index].routineHomeCalendarTextColor() }
        .count { textColor -> textColor == color }
}

internal fun ComposeTestRule.routineHomeCalendarColoredDateTotalCount(color: Color): Int = (1..MAX_DAY_OF_MONTH).sumOf { day -> routineHomeCalendarColoredDateCount(day = day, color = color) }

private fun SemanticsNode.routineHomeCalendarText(): String =
    config
        .getOrNull(SemanticsProperties.Text)
        .orEmpty()
        .joinToString(separator = "") { text -> text.text }

private fun SemanticsNodeInteraction.routineHomeCalendarTextColor(): Color {
    val textLayoutResults = mutableListOf<TextLayoutResult>()
    performSemanticsAction(SemanticsActions.GetTextLayoutResult) { action ->
        action(textLayoutResults)
    }

    return textLayoutResults
        .single()
        .layoutInput.style.color
}

private const val MAX_DAY_OF_MONTH = 31

@Composable
private fun RoutineHomeCalendarTestEffect(
    state: RoutineHomeScaffoldState,
    routineViewModel: RoutineHomeViewModel,
    calendarViewModel: RoutineHomeCalendarViewModel,
    holidayViewModel: RoutineHomeHolidayViewModel,
    homeReselectEvent: Flow<Unit>,
) {
    UpdateRoutineListTodayEffect(state = state.listState)
    UpdateRoutineHomeTodayEffect(
        routineViewModel = routineViewModel,
        state = state,
    )
    FetchCalendarRecordEffect(
        calendarViewModel = calendarViewModel,
        state = state,
    )
    FetchHolidayEffect(
        holidayViewModel = holidayViewModel,
        state = state,
    )
    ScrollToStartOnReselectEffect(
        reselectEvent = homeReselectEvent,
        state = state,
    )
    DismissUndoSnackbarEffect(
        keyProvider = { state.viewMode },
        hostState = state.snackbarHostState,
    )
}
