package io.github.taetae98coding.diary.compose.calendar.grid

import androidx.compose.foundation.layout.LayoutScopeMarker
import androidx.compose.runtime.Stable
import kotlinx.datetime.LocalDateRange

@LayoutScopeMarker
@Stable
public interface CalendarWeekOfMonthGridScope {
    public val dateRange: LocalDateRange

    public fun group(content: CalendarWeekOfMonthGridGroupScope.() -> Unit)
}
