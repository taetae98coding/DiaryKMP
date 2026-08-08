package io.github.taetae98coding.diary.compose.calendar.grid.verticalgrid

import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.calendar.grid.CalendarWeekOfMonthGridItemScope

internal class CalendarWeekOfMonthGridItemScopeVerticalGridImpl(
    private val scope: LazyGridItemScope,
) : CalendarWeekOfMonthGridItemScope {
    override fun Modifier.animateItem(): Modifier = with(scope) { this@animateItem.animateItem() }
}
