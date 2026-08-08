package io.github.taetae98coding.diary.compose.calendar.move

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.node.GlobalPositionAwareModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.unit.toSize
import kotlinx.datetime.LocalDateRange

public fun Modifier.calendarMoveItem(
    state: CalendarItemMoveState,
    key: Any,
    dateRange: LocalDateRange,
    onMoved: (LocalDateRange) -> Unit,
): Modifier =
    this then
        CalendarMoveItemElement(
            state = state,
            key = key,
            dateRange = dateRange,
            onMoved = onMoved,
        )

private data class CalendarMoveItemElement(
    private val state: CalendarItemMoveState,
    private val key: Any,
    private val dateRange: LocalDateRange,
    private val onMoved: (LocalDateRange) -> Unit,
) : ModifierNodeElement<CalendarMoveItemNode>() {
    override fun create(): CalendarMoveItemNode =
        CalendarMoveItemNode(
            state = state,
            key = key,
            dateRange = dateRange,
            onMoved = onMoved,
        )

    override fun update(node: CalendarMoveItemNode) {
        node.update(
            state = state,
            key = key,
            dateRange = dateRange,
            onMoved = onMoved,
        )
    }
}

internal class CalendarMoveItemNode(
    private var state: CalendarItemMoveState,
    key: Any,
    dateRange: LocalDateRange,
    onMoved: (LocalDateRange) -> Unit,
) : Modifier.Node(),
    GlobalPositionAwareModifierNode {
    private var target =
        CalendarMoveTarget(
            key = key,
            dateRange = dateRange,
            onMoved = onMoved,
        )

    override fun onAttach() {
        state.register(target)
    }

    override fun onDetach() {
        state.unregister(target)
    }

    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        target.bounds = Rect(offset = coordinates.positionInWindow(), size = coordinates.size.toSize())
    }

    fun update(
        state: CalendarItemMoveState,
        key: Any,
        dateRange: LocalDateRange,
        onMoved: (LocalDateRange) -> Unit,
    ) {
        val next =
            CalendarMoveTarget(
                key = key,
                dateRange = dateRange,
                onMoved = onMoved,
            ).also { it.bounds = target.bounds }

        if (isAttached) {
            this.state.unregister(target)
            state.register(next)
        }

        target = next
        this.state = state
    }
}
