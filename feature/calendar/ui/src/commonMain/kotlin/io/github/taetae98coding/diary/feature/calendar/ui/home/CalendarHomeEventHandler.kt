package io.github.taetae98coding.diary.feature.calendar.ui.home

import kotlinx.datetime.LocalDateRange
import kotlin.uuid.Uuid

internal fun handleCalendarHomeEvent(
    event: CalendarHomeScaffoldEvent,
    state: CalendarHomeScaffoldState,
    holidayViewModel: CalendarHomeHolidayViewModel,
    memoViewModel: CalendarHomeMemoViewModel,
    weatherViewModel: CalendarHomeWeatherViewModel,
    syncViewModel: CalendarHomeSyncViewModel,
    navigateToMemoDetail: (Uuid) -> Unit,
    navigateToMemoAdd: (LocalDateRange) -> Unit,
    navigateToContactDetail: (Uuid) -> Unit,
    navigateToFilter: () -> Unit,
) {
    when (event) {
        is CalendarHomeScaffoldEvent.ClickFilter -> navigateToFilter()

        is CalendarHomeScaffoldEvent.Refresh -> {
            syncViewModel.refresh()
            holidayViewModel.fetch(yearMonth = state.calendarState.currentYearMonth)
            weatherViewModel.fetch()
        }

        is CalendarHomeScaffoldEvent.ClickMemo -> navigateToMemoDetail(event.id)

        is CalendarHomeScaffoldEvent.ClickContactBirthday -> navigateToContactDetail(event.contactId)

        is CalendarHomeScaffoldEvent.MoveMemo ->
            memoViewModel.move(
                id = event.id,
                fromDateTime = event.fromDateTime,
                toDateRange = event.toDateRange,
            )

        is CalendarHomeScaffoldEvent.SelectDate -> {
            state.calendarSelectState.clear()
            navigateToMemoAdd(event.dateRange)
        }
    }
}
