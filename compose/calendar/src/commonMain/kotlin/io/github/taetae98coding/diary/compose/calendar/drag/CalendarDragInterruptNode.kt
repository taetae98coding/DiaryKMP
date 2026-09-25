package io.github.taetae98coding.diary.compose.calendar.drag

import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.saveable.SaveableStateRegistry
import androidx.compose.ui.Modifier
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.currentValueOf
import kotlinx.coroutines.launch

// 화면 재생성은 상태 저장 뒤 노드가 떨어지고, 앱이 백그라운드로 갈 때도 상태가 저장된다.
// 저장 뒤 끊긴 세션은 다음 프레임까지 완료를 미뤄, 그 사이 노드가 떨어지면 취소한다.
internal class CalendarDragInterruptNode :
    Modifier.Node(),
    CompositionLocalConsumerModifierNode {
    private var saveEntry: SaveableStateRegistry.Entry? = null
    private var isSavedDuringSession = false
    private var interruptedSession: CalendarDragSession? = null

    fun startSession() {
        isSavedDuringSession = false
    }

    fun interrupt(session: CalendarDragSession) {
        if (!isSavedDuringSession) {
            session.finish()
            return
        }

        interruptedSession = session
        coroutineScope.launch {
            interruptedSession?.finish()
            interruptedSession = null
        }
    }

    override fun onAttach() {
        saveEntry =
            currentValueOf(LocalSaveableStateRegistry)?.registerProvider(key = "$SAVE_KEY_PREFIX${hashCode()}") {
                isSavedDuringSession = true
                null
            }
    }

    // 노드가 떨어질 때의 합성 취소 이벤트는 이 노드를 위임한 노드에서 먼저 처리되므로, 그때 보류한 세션을 여기서 취소한다.
    override fun onDetach() {
        saveEntry?.unregister()
        saveEntry = null
        interruptedSession?.cancel()
        interruptedSession = null
    }
}

private const val SAVE_KEY_PREFIX = "CalendarDragInterruptNode:"
