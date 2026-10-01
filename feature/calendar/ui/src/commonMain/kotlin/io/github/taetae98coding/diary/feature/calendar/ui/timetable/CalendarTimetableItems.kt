package io.github.taetae98coding.diary.feature.calendar.ui.timetable

import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import io.github.taetae98coding.diary.compose.calendar.CalendarDefaults
import io.github.taetae98coding.diary.compose.calendar.text.CalendarText
import io.github.taetae98coding.diary.compose.timetable.TimetableScope
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.core.model.memo.CalendarMemo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.feature.calendar.ui.home.memo.toDateRange

internal fun TimetableScope.calendarTimetableItems(
    memoProvider: () -> List<CalendarMemo>,
    holidayProvider: () -> List<Holiday>,
    holidayNameColor: Color,
    nonHolidayNameColor: Color,
    onEvent: (CalendarTimetableScaffoldEvent) -> Unit,
    onHolidayClick: (Holiday) -> Unit,
) {
    memoItems(memoProvider = memoProvider, onEvent = onEvent)
    holidayItems(
        holidayProvider = holidayProvider,
        holidayNameColor = holidayNameColor,
        nonHolidayNameColor = nonHolidayNameColor,
        onHolidayClick = onHolidayClick,
    )
}

private fun TimetableScope.memoItems(
    memoProvider: () -> List<CalendarMemo>,
    onEvent: (CalendarTimetableScaffoldEvent) -> Unit,
) {
    memoProvider().forEach { memo ->
        val dateTime = memo.dateTime
        val color = Color(color = memo.color.toInt())
        val onClick = { onEvent(CalendarTimetableScaffoldEvent.ClickMemo(id = memo.id)) }

        if (dateTime is MemoDateTime.DateTime) {
            timeItem(
                start = dateTime.start,
                endInclusive = dateTime.endInclusive,
                key = memo.id,
            ) {
                CalendarTimetableMemoBlock(
                    title = memo.title,
                    color = color,
                    onClick = onClick,
                )
            }
        } else {
            allDayItem(
                dateRange = dateTime.toDateRange(),
                key = memo.id,
            ) {
                CalendarText(
                    text = memo.title,
                    modifier =
                        Modifier
                            .clip(CalendarDefaults.itemShape)
                            .clickable(role = Role.Button, onClick = onClick),
                    color = color,
                )
            }
        }
    }
}

private fun TimetableScope.holidayItems(
    holidayProvider: () -> List<Holiday>,
    holidayNameColor: Color,
    nonHolidayNameColor: Color,
    onHolidayClick: (Holiday) -> Unit,
) {
    holidayProvider().forEach { holiday ->
        allDayItem(
            dateRange = holiday.dateRange,
            key = holiday.toString(),
        ) {
            CalendarText(
                text = holiday.name,
                modifier =
                    Modifier
                        .clip(CalendarDefaults.itemShape)
                        .clickable(role = Role.Button) { onHolidayClick(holiday) },
                color = if (holiday.isHoliday) holidayNameColor else nonHolidayNameColor,
            )
        }
    }
}
