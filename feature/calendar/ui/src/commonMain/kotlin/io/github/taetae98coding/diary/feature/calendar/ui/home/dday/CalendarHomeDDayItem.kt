package io.github.taetae98coding.diary.feature.calendar.ui.home.dday

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.taetae98coding.diary.compose.calendar.dday.CalendarDDayText
import io.github.taetae98coding.diary.compose.calendar.dday.itemKey
import io.github.taetae98coding.diary.compose.calendar.dday.toDateRange
import io.github.taetae98coding.diary.compose.calendar.grid.CalendarWeekOfMonthGridGroupScope
import io.github.taetae98coding.diary.compose.calendar.move.CalendarItemMoveState
import io.github.taetae98coding.diary.compose.calendar.move.calendarLongPressIgnoredItem
import io.github.taetae98coding.diary.core.model.dday.CalendarDDay
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeScaffoldEvent

internal fun CalendarWeekOfMonthGridGroupScope.ddayItem(
    moveState: CalendarItemMoveState,
    ddayProvider: () -> List<CalendarDDay>,
    onEvent: (CalendarHomeScaffoldEvent) -> Unit,
) {
    ddayProvider().forEach { dday ->
        item(
            dateRange = dday.toDateRange(),
            key = dday.itemKey(),
        ) {
            CalendarDDayText(
                title = dday.title,
                label = dday.label,
                color = Color(color = dday.color.toInt()),
                onClick = { onEvent(CalendarHomeScaffoldEvent.ClickDDay(id = dday.ddayId)) },
                modifier =
                    Modifier
                        .animateItem()
                        .calendarLongPressIgnoredItem(state = moveState),
            )
        }
    }
}
