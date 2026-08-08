package io.github.taetae98coding.diary.compose.calendar.select

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.unit.IntSize
import io.github.taetae98coding.diary.compose.calendar.drag.CalendarDragNode
import io.github.taetae98coding.diary.library.kotlinx.datetime.DAYS_PER_WEEK
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.plus

public fun Modifier.calendarWeekSelectDrag(
    state: CalendarSelectState,
    firstSunday: LocalDate,
    weekCount: Int,
    onSelect: (LocalDateRange) -> Unit,
): Modifier =
    this then
        CalendarWeekSelectDragElement(
            state = state,
            firstSunday = firstSunday,
            weekCount = weekCount,
            onSelect = onSelect,
        )

private data class CalendarWeekSelectDragElement(
    private val state: CalendarSelectState,
    private val firstSunday: LocalDate,
    private val weekCount: Int,
    private val onSelect: (LocalDateRange) -> Unit,
) : ModifierNodeElement<CalendarDragNode>() {
    override fun create(): CalendarDragNode =
        CalendarDragNode(
            selectState = state,
            moveState = null,
            dateAt = { position, size -> firstSunday.dateAt(position = position, size = size, weekCount = weekCount) },
            animateScrollMonth = null,
            onSelect = onSelect,
        )

    override fun update(node: CalendarDragNode) {
        node.update(
            selectState = state,
            moveState = null,
            dateAt = { position, size -> firstSunday.dateAt(position = position, size = size, weekCount = weekCount) },
            animateScrollMonth = null,
            onSelect = onSelect,
        )
    }
}

private fun LocalDate.dateAt(
    position: Offset,
    size: IntSize,
    weekCount: Int,
): LocalDate {
    val lastWeekIndex = (weekCount - 1).coerceAtLeast(0)
    val dayOfWeekNumber = (position.x / (size.width.toFloat() / DAYS_PER_WEEK)).toInt().coerceIn(0, DAYS_PER_WEEK - 1)
    val weekIndex = (position.y / (size.height.toFloat() / (lastWeekIndex + 1))).toInt().coerceIn(0, lastWeekIndex)

    return plus(weekIndex * DAYS_PER_WEEK + dayOfWeekNumber, DateTimeUnit.DAY)
}
