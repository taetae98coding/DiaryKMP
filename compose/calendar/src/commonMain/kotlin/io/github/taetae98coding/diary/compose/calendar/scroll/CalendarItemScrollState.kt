package io.github.taetae98coding.diary.compose.calendar.scroll

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Stable
public class CalendarItemScrollState {
    private val gridStateList = mutableListOf<LazyGridState>()

    public suspend fun scrollToTop() {
        coroutineScope {
            gridStateList.toList().forEach { gridState ->
                launch { gridState.scrollToItem(index = 0) }
            }
        }
    }

    internal fun register(gridState: LazyGridState) {
        gridStateList += gridState
    }

    internal fun unregister(gridState: LazyGridState) {
        gridStateList -= gridState
    }
}

@Composable
public fun rememberCalendarItemScrollState(): CalendarItemScrollState = remember { CalendarItemScrollState() }
