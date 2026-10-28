package io.github.taetae98coding.diary.feature.calendar.ui.home

import androidx.compose.ui.graphics.Color
import io.github.taetae98coding.diary.compose.calendar.grid.CalendarWeekOfMonthGridScope
import io.github.taetae98coding.diary.compose.calendar.move.CalendarItemMoveState
import io.github.taetae98coding.diary.core.model.contact.CalendarContactBirthday
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.core.model.memo.CalendarMemo
import io.github.taetae98coding.diary.core.model.weather.CalendarWeather
import io.github.taetae98coding.diary.feature.calendar.ui.home.birthday.birthdayItem
import io.github.taetae98coding.diary.feature.calendar.ui.home.holiday.holidayItem
import io.github.taetae98coding.diary.feature.calendar.ui.home.memo.memoItem
import io.github.taetae98coding.diary.feature.calendar.ui.home.weather.weatherItem

@Suppress("LongParameterList")
internal fun CalendarWeekOfMonthGridScope.calendarHomeItems(
    moveState: CalendarItemMoveState,
    weatherProvider: () -> List<CalendarWeather>,
    holidayProvider: () -> List<Holiday>,
    holidayNameColor: Color,
    nonHolidayNameColor: Color,
    memoProvider: () -> List<CalendarMemo>,
    birthdayProvider: () -> List<CalendarContactBirthday>,
    birthdayColor: Color,
    onWeatherClick: () -> Unit,
    onHolidayClick: (Holiday) -> Unit,
    onEvent: (CalendarHomeScaffoldEvent) -> Unit,
) {
    group {
        weatherItem(
            onWeatherClick = onWeatherClick,
            weatherProvider = weatherProvider,
        )
    }
    group {
        memoItem(
            moveState = moveState,
            memoProvider = memoProvider,
            onEvent = onEvent,
        )
        birthdayItem(
            birthdayProvider = birthdayProvider,
            birthdayColor = birthdayColor,
            onBirthdayClick = { birthday ->
                onEvent(CalendarHomeScaffoldEvent.ClickContactBirthday(contactId = birthday.contactId))
            },
        )
        holidayItem(
            holidayProvider = holidayProvider,
            holidayNameColor = holidayNameColor,
            nonHolidayNameColor = nonHolidayNameColor,
            onHolidayClick = onHolidayClick,
        )
    }
}
