package io.github.taetae98coding.diary.compose.calendar.drag

import androidx.compose.ui.geometry.Offset
import kotlinx.datetime.LocalDate

internal interface CalendarDragSession {
    fun drag(
        date: LocalDate,
        windowPosition: Offset?,
    ): Boolean

    fun finish()

    fun cancel()
}
