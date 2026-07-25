package io.github.taetae98coding.diary.compose.core.dialog

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import io.github.taetae98coding.diary.library.compose.ui.color.parseHexColorOrNull
import io.github.taetae98coding.diary.library.compose.ui.color.toHexString
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch

@Composable
internal fun ColorPickerSyncEffect(state: ColorPickerState) {
    LaunchedEffect(state) {
        launch {
            snapshotFlow { state.hexTextFieldState.text.toString() }
                .collect { text ->
                    if (state.isAnimating) {
                        return@collect
                    }

                    val color =
                        parseHexColorOrNull(text)
                            ?.takeIf { it != state.color }
                            ?: return@collect

                    try {
                        state.animateColorTo(value = color)
                    } catch (_: CancellationException) {
                        // 무작위 컬러 버튼이 진행 중인 애니메이션을 취소해도 Hex 입력 수집은 유지한다.
                        currentCoroutineContext().ensureActive()
                    }
                }
        }

        launch {
            snapshotFlow { state.color }
                .collect { color ->
                    val hexColor = parseHexColorOrNull(state.hexTextFieldState.text)

                    if (hexColor != color && hexColor != state.targetColor) {
                        state.hexTextFieldState.setTextAndPlaceCursorAtEnd(color.toHexString())
                    }
                }
        }
    }
}
