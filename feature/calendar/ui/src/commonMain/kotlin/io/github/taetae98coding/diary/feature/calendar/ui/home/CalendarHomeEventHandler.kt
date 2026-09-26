package io.github.taetae98coding.diary.feature.calendar.ui.home

import io.github.taetae98coding.diary.compose.calendar.CalendarEvent
import io.github.taetae98coding.diary.feature.calendar.api.CalendarTimetableNavKey
import io.github.taetae98coding.diary.feature.calendar.ui.home.birthday.toDateRange
import io.github.taetae98coding.diary.feature.calendar.ui.home.holiday.CalendarHomeHolidayViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.home.memo.CalendarHomeMemoViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.home.memo.toDateRange
import io.github.taetae98coding.diary.feature.calendar.ui.home.weather.CalendarHomeWeatherViewModel
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
    }
}

internal fun handleCalendarHomeCalendarEvent(
    event: CalendarEvent,
    state: CalendarHomeScaffoldState,
    navigateToMemoAdd: (LocalDateRange) -> Unit,
    navigateToTimetable: (CalendarTimetableNavKey) -> Unit,
) {
    when (event) {
        is CalendarEvent.Select -> {
            state.calendarSelectState.clear()
            navigateToMemoAdd(event.dateRange)
        }

        is CalendarEvent.ClickDate ->
            navigateToTimetable(CalendarTimetableNavKey(type = CalendarTimetableNavKey.Type.DAY, date = event.date))

        is CalendarEvent.ClickWeek ->
            navigateToTimetable(CalendarTimetableNavKey(type = CalendarTimetableNavKey.Type.WEEK, date = event.startDate))
    }
}
