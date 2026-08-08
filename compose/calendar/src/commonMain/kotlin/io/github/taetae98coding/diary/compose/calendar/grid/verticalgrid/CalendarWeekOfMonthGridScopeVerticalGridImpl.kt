package io.github.taetae98coding.diary.compose.calendar.grid.verticalgrid

import io.github.taetae98coding.diary.compose.calendar.grid.CalendarWeekOfMonthGridGroupScope
import io.github.taetae98coding.diary.compose.calendar.grid.CalendarWeekOfMonthGridScope
import kotlinx.datetime.LocalDateRange

internal class CalendarWeekOfMonthGridScopeVerticalGridImpl(
    override val dateRange: LocalDateRange,
) : CalendarWeekOfMonthGridScope {
    val groupList: List<CalendarWeekOfMonthGridGroupScopeVerticalGridImpl>
        field = mutableListOf<CalendarWeekOfMonthGridGroupScopeVerticalGridImpl>()

    override fun group(content: CalendarWeekOfMonthGridGroupScope.() -> Unit) {
        groupList.add(CalendarWeekOfMonthGridGroupScopeVerticalGridImpl(dateRange).apply(content))
    }
}
