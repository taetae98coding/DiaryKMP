package io.github.taetae98coding.diary.compose.calendar.select

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.datetime.LocalDateRange

@Stable
public class CalendarSelectState {
    public var dateRange: LocalDateRange? by mutableStateOf(null)
        private set

    public fun select(dateRange: LocalDateRange) {
        this.dateRange = dateRange
    }

    public fun clear() {
        dateRange = null
    }
}

@Composable
public fun rememberCalendarSelectState(): CalendarSelectState = remember { CalendarSelectState() }
