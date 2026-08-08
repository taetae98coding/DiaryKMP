package io.github.taetae98coding.diary.compose.calendar.grid.verticalgrid

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.calendar.dateRangeAt
import io.github.taetae98coding.diary.compose.calendar.grid.CalendarWeekOfMonthGridScope
import io.github.taetae98coding.diary.compose.calendar.scroll.CalendarItemScrollState
import io.github.taetae98coding.diary.compose.calendar.scroll.rememberCalendarItemScrollState
import io.github.taetae98coding.diary.compose.calendar.text.CalendarBarText
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.library.kotlinx.datetime.DAYS_PER_WEEK
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus

@Composable
internal fun CalendarWeekOfMonthLazyVerticalGrid(
    yearMonth: YearMonth,
    weekOfMonth: Int,
    modifier: Modifier = Modifier,
    itemScrollState: CalendarItemScrollState = rememberCalendarItemScrollState(),
    content: CalendarWeekOfMonthGridScope.() -> Unit,
) {
    val dateRange =
        remember(yearMonth, weekOfMonth) {
            yearMonth.dateRangeAt(weekOfMonth = weekOfMonth)
        }
    val gridState = rememberLazyGridState()

    DisposableEffect(itemScrollState, gridState) {
        itemScrollState.register(gridState)
        onDispose { itemScrollState.unregister(gridState) }
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(DAYS_PER_WEEK),
        modifier = modifier,
        state = gridState,
        contentPadding = PaddingValues(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        CalendarWeekOfMonthGridScopeVerticalGridImpl(dateRange)
            .apply(content)
            .groupList
            .flatMap { it.itemGrid() }
            .forEach { list ->
                var cursor = dateRange.start
                list.forEach { item ->
                    val prefixSpan = cursor.daysUntil(item.dateRange.start)
                    if (prefixSpan > 0) {
                        item(span = { GridItemSpan(prefixSpan) }) { }
                    }
                    item(
                        key = item.key,
                        span = { GridItemSpan(item.dateRange.size) },
                    ) {
                        item.content(CalendarWeekOfMonthGridItemScopeVerticalGridImpl(this))
                    }
                    cursor = item.dateRange.endInclusive.plus(1, DateTimeUnit.DAY)
                }
                val postfixSpan = cursor.daysUntil(dateRange.endInclusive) + 1
                if (postfixSpan > 0) {
                    item(span = { GridItemSpan(postfixSpan) }) { }
                }
            }
    }
}

@ComponentPreview
@Composable
private fun CalendarWeekOfMonthLazyVerticalGridPreview() {
    DiaryTheme {
        Surface {
            CalendarWeekOfMonthLazyVerticalGrid(
                yearMonth = YearMonth(year = 2026, month = 7),
                weekOfMonth = 2,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(96.dp),
            ) {
                group {
                    item(dateRange = LocalDate(year = 2026, month = 7, day = 13)..LocalDate(year = 2026, month = 7, day = 15)) {
                        CalendarBarText(text = "메모 제목")
                    }
                }
            }
        }
    }
}
