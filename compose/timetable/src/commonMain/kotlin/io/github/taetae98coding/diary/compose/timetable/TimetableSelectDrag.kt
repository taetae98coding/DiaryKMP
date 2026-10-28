package io.github.taetae98coding.diary.compose.timetable

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.SuspendingPointerInputModifierNode
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DelegatingNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntSize

internal fun Modifier.timetableSelectDrag(
    state: TimetableSelectState,
    resolver: TimetableSelectionResolver,
    onEvent: (TimetableEvent) -> Unit,
): Modifier = this then TimetableSelectDragElement(state = state, resolver = resolver, onEvent = onEvent)

private data class TimetableSelectDragElement(
    private val state: TimetableSelectState,
    private val resolver: TimetableSelectionResolver,
    private val onEvent: (TimetableEvent) -> Unit,
) : ModifierNodeElement<TimetableSelectDragNode>() {
    override fun create(): TimetableSelectDragNode = TimetableSelectDragNode(state = state, resolver = resolver, onEvent = onEvent)

    override fun update(node: TimetableSelectDragNode) {
        node.update(state = state, resolver = resolver, onEvent = onEvent)
    }
}

private class TimetableSelectDragNode(
    private var state: TimetableSelectState,
    private var resolver: TimetableSelectionResolver,
    private var onEvent: (TimetableEvent) -> Unit,
) : DelegatingNode(),
    CompositionLocalConsumerModifierNode {
    private val pointerInputNode = delegate(SuspendingPointerInputModifierNode { detectSelect() })

    fun update(
        state: TimetableSelectState,
        resolver: TimetableSelectionResolver,
        onEvent: (TimetableEvent) -> Unit,
    ) {
        this.resolver = resolver
        this.onEvent = onEvent

        if (this.state !== state) {
            this.state.clear()
            this.state = state
            pointerInputNode.resetPointerInputHandler()
        }
    }

    private suspend fun PointerInputScope.detectSelect() {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            val longPress = awaitLongPressOrCancellation(down.id) ?: return@awaitEachGesture
            val selection = resolver.resolve(position = longPress.position, size = size, anchor = null) ?: return@awaitEachGesture
            val selectState = state
            var isReleased = false

            selectState.select(selection)
            try {
                performHapticFeedback(HapticFeedbackType.LongPress)
                isReleased = awaitRelease(longPress = longPress)
            } finally {
                selectState.end(isReleased = isReleased, onEvent = onEvent)
            }
        }
    }

    private suspend fun AwaitPointerEventScope.awaitRelease(longPress: PointerInputChange): Boolean {
        var change = longPress
        var isCanceled = false

        while (!change.changedToUpIgnoreConsumed()) {
            val event = awaitPointerEvent(pass = PointerEventPass.Initial)
            val next = event.changes.firstOrNull { it.id == longPress.id } ?: return false

            // 시스템이 제스처를 끊으면 Compose는 이미 소비된 손 뗌 이벤트를 합성해 보낸다.
            isCanceled = next.changedToUpIgnoreConsumed() && next.isConsumed
            // 선택하는 동안 페이지 넘김, 세로 스크롤과 아이템 누름이 일어나지 않도록 모든 손가락을 먼저 소비한다.
            event.changes.forEach { it.consume() }
            if (!next.changedToUpIgnoreConsumed()) {
                val selection = resolver.resolve(position = next.position, size = size, anchor = state.selection)

                if (selection != null && selection != state.selection) {
                    state.select(selection)
                    performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
                }
            }

            change = next
        }

        return !isCanceled
    }

    private fun performHapticFeedback(type: HapticFeedbackType) {
        currentValueOf(LocalHapticFeedback).performHapticFeedback(type)
    }
}

private fun TimetableSelectState.end(
    isReleased: Boolean,
    onEvent: (TimetableEvent) -> Unit,
) {
    val selection = selection ?: return

    clear()
    if (isReleased) {
        onEvent(selection.toEvent())
    }
}
