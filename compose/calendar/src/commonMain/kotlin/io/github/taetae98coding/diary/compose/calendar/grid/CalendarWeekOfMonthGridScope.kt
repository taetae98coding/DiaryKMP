package io.github.taetae98coding.diary.compose.calendar.grid

import androidx.compose.foundation.layout.LayoutScopeMarker
import androidx.compose.runtime.Stable

@LayoutScopeMarker
@Stable
public interface CalendarWeekOfMonthGridScope {
    public fun group(content: CalendarWeekOfMonthGridGroupScope.() -> Unit)
}
