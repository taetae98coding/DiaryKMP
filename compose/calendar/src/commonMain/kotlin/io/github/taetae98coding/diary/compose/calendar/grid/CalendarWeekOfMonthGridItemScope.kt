package io.github.taetae98coding.diary.compose.calendar.grid

import androidx.compose.foundation.layout.LayoutScopeMarker
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier

@LayoutScopeMarker
@Stable
public interface CalendarWeekOfMonthGridItemScope {
    public fun Modifier.animateItem(): Modifier
}
