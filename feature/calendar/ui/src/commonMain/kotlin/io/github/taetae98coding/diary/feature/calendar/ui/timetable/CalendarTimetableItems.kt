package io.github.taetae98coding.diary.feature.calendar.ui.timetable

import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import io.github.taetae98coding.diary.compose.calendar.CalendarDefault
import io.github.taetae98coding.diary.compose.calendar.text.CalendarText
import io.github.taetae98coding.diary.compose.timetable.TimetableScope
import io.github.taetae98coding.diary.core.model.memo.CalendarMemo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.feature.calendar.ui.home.memo.isSingleDayDateTime
import io.github.taetae98coding.diary.feature.calendar.ui.home.memo.toDateRange

internal fun TimetableScope.calendarTimetableItems(
    memoProvider: () -> List<CalendarMemo>,
    onEvent: (CalendarTimetableScaffoldEvent) -> Unit,
) {
    memoProvider().forEach { memo ->
        val dateTime = memo.dateTime
        val color = Color(color = memo.color.toInt())
        val onClick = { onEvent(CalendarTimetableScaffoldEvent.ClickMemo(id = memo.id)) }

        if (dateTime is MemoDateTime.DateTime && dateTime.isSingleDayDateTime()) {
            timeItem(
                date = dateTime.start.date,
                startTime = dateTime.start.time,
                endTime = dateTime.endInclusive.time,
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
                            .clip(CalendarDefault.itemShape)
                            .clickable(role = Role.Button, onClick = onClick),
                    color = color,
                )
            }
        }
    }
}
