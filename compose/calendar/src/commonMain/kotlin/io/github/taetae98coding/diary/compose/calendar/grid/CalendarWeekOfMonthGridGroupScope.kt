package io.github.taetae98coding.diary.compose.calendar.grid

import androidx.compose.foundation.layout.LayoutScopeMarker
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import kotlinx.datetime.LocalDateRange

@LayoutScopeMarker
@Stable
public interface CalendarWeekOfMonthGridGroupScope {
    public fun item(
        dateRange: LocalDateRange,
        key: Any? = null,
        content: @Composable CalendarWeekOfMonthGridItemScope.() -> Unit,
    )
}
