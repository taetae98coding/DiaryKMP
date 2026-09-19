package io.github.taetae98coding.diary.compose.calendar.grid.verticalgrid

import androidx.compose.runtime.Composable
import io.github.taetae98coding.diary.compose.calendar.grid.CalendarWeekOfMonthGridGroupScope
import io.github.taetae98coding.diary.compose.calendar.grid.CalendarWeekOfMonthGridItem
import io.github.taetae98coding.diary.compose.calendar.grid.CalendarWeekOfMonthGridItemScope
import io.github.taetae98coding.diary.library.kotlinx.datetime.overlaps
import kotlinx.datetime.LocalDateRange

internal class CalendarWeekOfMonthGridGroupScopeVerticalGridImpl(
    private val dateRange: LocalDateRange,
) : CalendarWeekOfMonthGridGroupScope {
    private val itemList = mutableListOf<CalendarWeekOfMonthGridItem>()

    override fun item(
        dateRange: LocalDateRange,
        key: Any?,
        content: @Composable (CalendarWeekOfMonthGridItemScope.() -> Unit),
    ) {
        if (!dateRange.overlaps(this.dateRange)) return

        itemList.add(
            CalendarWeekOfMonthGridItem(
                key = key,
                dateRange = dateRange,
                content = content,
            ),
        )
    }

    fun itemGrid(): List<List<CalendarWeekOfMonthGridItem>> {
        val grid = mutableListOf<List<CalendarWeekOfMonthGridItem>>()

        itemList
            .sortedWith(
                compareBy<CalendarWeekOfMonthGridItem> { it.dateRange.clipToWeek().start }
                    .thenByDescending { it.dateRange.clipToWeek().endInclusive }
                    .thenBy { it.dateRange.start }
                    .thenByDescending { it.dateRange.endInclusive },
            ).map { it.copy(dateRange = it.dateRange.clipToWeek()) }
            .forEach { item ->
                val rowIndex = grid.indexOfFirst { list -> list.none { it.dateRange.overlaps(item.dateRange) } }
                if (rowIndex == -1) {
                    grid.add(listOf(item))
                } else {
                    grid[rowIndex] =
                        buildList {
                            addAll(grid[rowIndex])
                            add(item)
                        }
                }
            }

        return grid.toList()
    }

    private fun LocalDateRange.clipToWeek(): LocalDateRange = start.coerceIn(dateRange)..endInclusive.coerceIn(dateRange)
}
