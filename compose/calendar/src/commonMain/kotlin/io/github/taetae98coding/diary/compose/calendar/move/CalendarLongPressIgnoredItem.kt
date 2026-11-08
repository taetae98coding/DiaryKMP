package io.github.taetae98coding.diary.compose.calendar.move

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.node.GlobalPositionAwareModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.unit.toSize

public fun Modifier.calendarLongPressIgnoredItem(state: CalendarItemMoveState): Modifier = this then CalendarLongPressIgnoredItemElement(state = state)

private data class CalendarLongPressIgnoredItemElement(
    private val state: CalendarItemMoveState,
) : ModifierNodeElement<CalendarLongPressIgnoredItemNode>() {
    override fun create(): CalendarLongPressIgnoredItemNode = CalendarLongPressIgnoredItemNode(state = state)

    override fun update(node: CalendarLongPressIgnoredItemNode) {
        node.update(state = state)
    }
}

internal class CalendarLongPressIgnoredItemNode(
    private var state: CalendarItemMoveState,
) : Modifier.Node(),
    GlobalPositionAwareModifierNode {
    private val target = CalendarLongPressIgnoredTarget()

    override fun onAttach() {
        state.register(target)
    }

    override fun onDetach() {
        state.unregister(target)
    }

    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        target.bounds = Rect(offset = coordinates.positionInWindow(), size = coordinates.size.toSize())
    }

    fun update(state: CalendarItemMoveState) {
        if (this.state === state) return

        if (isAttached) {
            this.state.unregister(target)
            state.register(target)
        }

        this.state = state
    }
}
