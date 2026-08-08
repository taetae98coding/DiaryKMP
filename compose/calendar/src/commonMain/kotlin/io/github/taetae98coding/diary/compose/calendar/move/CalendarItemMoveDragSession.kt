package io.github.taetae98coding.diary.compose.calendar.move

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.toSize
import io.github.taetae98coding.diary.compose.calendar.drag.CalendarDragSession
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus

internal class CalendarItemMoveDragSession(
    private val state: CalendarItemMoveState,
    private val moving: CalendarMovingItem,
) : CalendarDragSession {
    private var anchor: LocalDate? = null

    override fun drag(
        date: LocalDate,
        windowPosition: Offset?,
    ): Boolean {
        windowPosition?.let { state.pointerPosition = it }

        val anchor = anchor ?: date.also { this.anchor = it }
        val offset = anchor.daysUntil(date)
        val toDateRange =
            moving.fromDateRange.start.plus(offset, DateTimeUnit.DAY)..moving.fromDateRange.endInclusive.plus(offset, DateTimeUnit.DAY)

        if (moving.toDateRange == toDateRange) return false

        moving.toDateRange = toDateRange
        return true
    }

    override fun finish() {
        state.moving = null
        if (moving.toDateRange != moving.fromDateRange) {
            moving.onMoved(moving.toDateRange)
        }
    }

    override fun cancel() {
        state.moving = null
    }
}

internal fun CalendarItemMoveState.startDragSession(
    coordinates: LayoutCoordinates,
    position: Offset,
): CalendarDragSession? {
    val windowPosition = coordinates.localToWindow(position)
    val target = findTarget(windowPosition = windowPosition) ?: return null
    val calendarBounds = Rect(offset = coordinates.positionInWindow(), size = coordinates.size.toSize())
    val moving =
        CalendarMovingItem(
            key = target.key,
            fromDateRange = target.dateRange,
            pieceBoundsList = findPieceBoundsList(key = target.key, bounds = calendarBounds),
            startPointerPosition = windowPosition,
            onMoved = target.onMoved,
        )

    this.moving = moving
    pointerPosition = windowPosition

    return CalendarItemMoveDragSession(state = this, moving = moving)
}
