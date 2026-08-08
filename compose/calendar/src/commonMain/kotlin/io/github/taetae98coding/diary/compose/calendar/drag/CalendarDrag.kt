package io.github.taetae98coding.diary.compose.calendar.drag

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.unit.IntSize
import io.github.taetae98coding.diary.compose.calendar.CalendarState
import io.github.taetae98coding.diary.compose.calendar.WEEKS_PER_MONTH
import io.github.taetae98coding.diary.compose.calendar.dateAt
import io.github.taetae98coding.diary.library.kotlinx.datetime.DAYS_PER_WEEK
import io.github.taetae98coding.diary.library.kotlinx.datetime.sundayBasedDayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.YearMonth

public fun Modifier.calendarDrag(
    state: CalendarState,
    onSelect: (LocalDateRange) -> Unit,
): Modifier = this then CalendarDragElement(state = state, onSelect = onSelect)

private data class CalendarDragElement(
    private val state: CalendarState,
    private val onSelect: (LocalDateRange) -> Unit,
) : ModifierNodeElement<CalendarDragNode>() {
    override fun create(): CalendarDragNode =
        CalendarDragNode(
            selectState = state.selectState,
            moveState = state.moveState,
            dateAt = { position, size -> state.dateAt(position = position, size = size) },
            animateScrollMonth = { direction -> state.animateScrollMonth(direction = direction) },
            onSelect = onSelect,
        )

    override fun update(node: CalendarDragNode) {
        node.update(
            selectState = state.selectState,
            moveState = state.moveState,
            dateAt = { position, size -> state.dateAt(position = position, size = size) },
            animateScrollMonth = { direction -> state.animateScrollMonth(direction = direction) },
            onSelect = onSelect,
        )
    }
}

private fun CalendarState.dateAt(
    position: Offset,
    size: IntSize,
): LocalDate = currentYearMonth.dateAt(position = position, size = size)

private suspend fun CalendarState.animateScrollMonth(direction: Int): Boolean {
    val before = currentYearMonth

    if (direction < 0) {
        animateScrollToPreviousMonth()
    } else {
        animateScrollToNextMonth()
    }

    return currentYearMonth != before
}

private fun YearMonth.dateAt(
    position: Offset,
    size: IntSize,
): LocalDate {
    val dayOfWeekNumber = (position.x / (size.width.toFloat() / DAYS_PER_WEEK)).toInt().coerceIn(0, DAYS_PER_WEEK - 1)
    val weekOfMonth = (position.y / (size.height.toFloat() / WEEKS_PER_MONTH)).toInt().coerceIn(0, WEEKS_PER_MONTH - 1)

    return dateAt(weekOfMonth = weekOfMonth, dayOfWeek = sundayBasedDayOfWeek(number = dayOfWeekNumber))
}
