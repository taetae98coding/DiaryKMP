package io.github.taetae98coding.diary.compose.timetable

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.node.GlobalPositionAwareModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.unit.toSize

internal fun Modifier.timetableLongPressIgnoredItem(state: TimetableSelectState): Modifier = this then TimetableLongPressIgnoredItemElement(state = state)

private data class TimetableLongPressIgnoredItemElement(
    private val state: TimetableSelectState,
) : ModifierNodeElement<TimetableLongPressIgnoredItemNode>() {
    override fun create(): TimetableLongPressIgnoredItemNode = TimetableLongPressIgnoredItemNode(state = state)

    override fun update(node: TimetableLongPressIgnoredItemNode) {
        node.update(state = state)
    }
}

private class TimetableLongPressIgnoredItemNode(
    private var state: TimetableSelectState,
) : Modifier.Node(),
    GlobalPositionAwareModifierNode {
    private val target = TimetableLongPressIgnoredTarget()

    override fun onAttach() {
        state.register(target)
    }

    override fun onDetach() {
        state.unregister(target)
    }

    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        target.bounds = Rect(offset = coordinates.positionInWindow(), size = coordinates.size.toSize())
    }

    fun update(state: TimetableSelectState) {
        if (this.state === state) return

        if (isAttached) {
            this.state.unregister(target)
            state.register(target)
        }

        this.state = state
    }
}
