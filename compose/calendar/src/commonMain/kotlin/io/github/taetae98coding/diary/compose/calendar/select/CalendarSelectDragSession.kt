package io.github.taetae98coding.diary.compose.calendar.select

import androidx.compose.ui.geometry.Offset
import io.github.taetae98coding.diary.compose.calendar.drag.CalendarDragSession
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange

internal class CalendarSelectDragSession(
    private val state: CalendarSelectState,
    private val onSelect: (LocalDateRange) -> Unit,
) : CalendarDragSession {
    private var anchor: LocalDate? = null

    override fun drag(
        date: LocalDate,
        windowPosition: Offset?,
    ): Boolean {
        val anchor = anchor ?: date.also { this.anchor = it }
        val dateRange = minOf(anchor, date)..maxOf(anchor, date)

        if (state.dateRange == dateRange) return false

        state.select(dateRange)
        return true
    }

    override fun finish() {
        state.dateRange?.let { dateRange -> onSelect(dateRange) }
    }

    override fun cancel() {
        state.clear()
    }
}
