package io.github.taetae98coding.diary.feature.routine.ui.home.press

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.SuspendingPointerInputModifierNode
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.node.DelegatingNode
import androidx.compose.ui.node.ModifierNodeElement

// Material 버튼과 clickable은 길게 누르기를 받지 않고 combinedClickable은 키를 오래 눌러도 길게 누르기로 보므로, 포인터의 길게 누르기만 따로 인식한다.
// 누름 상호작용(PressInteraction)은 스크롤 영역 안에서 늦게 시작되므로 시간을 재는 기준으로 쓰지 않는다.
// 인식한 뒤의 손 뗌이 누르기로 이어지지 않도록 clickable보다 앞에 붙인다.
internal fun Modifier.longPress(onLongPress: () -> Unit): Modifier = this then LongPressElement(onLongPress = onLongPress)

private data class LongPressElement(
    private val onLongPress: () -> Unit,
) : ModifierNodeElement<LongPressNode>() {
    override fun create(): LongPressNode = LongPressNode(onLongPress = onLongPress)

    override fun update(node: LongPressNode) {
        node.onLongPress = onLongPress
    }
}

private class LongPressNode(
    var onLongPress: () -> Unit,
) : DelegatingNode() {
    init {
        delegate(SuspendingPointerInputModifierNode { detectLongPress() })
    }

    private suspend fun PointerInputScope.detectLongPress() {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            // 보조 클릭은 누른 채로 유지해도 길게 누르기로 보지 않는다.
            if (currentEvent.buttons.isSecondaryPressed) return@awaitEachGesture
            val longPress = awaitLongPressOrCancellation(down.id) ?: return@awaitEachGesture

            onLongPress()
            awaitConsumedRelease(longPress = longPress)
        }
    }
}

private suspend fun AwaitPointerEventScope.awaitConsumedRelease(longPress: PointerInputChange) {
    do {
        val event = awaitPointerEvent(pass = PointerEventPass.Initial)
        val next = event.changes.firstOrNull { it.id == longPress.id }

        // 안쪽 clickable의 누르기와 바깥의 스와이프가 이어지지 않도록 손을 뗄 때까지 모든 손가락을 먼저 소비한다.
        event.changes.forEach { it.consume() }
    } while (next != null && !next.changedToUpIgnoreConsumed())
}
