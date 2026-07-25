package io.github.taetae98coding.diary.compose.core.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

@Stable
public class DialogState(
    isVisible: Boolean = false,
) {
    public var isVisible: Boolean by mutableStateOf(isVisible)
        private set

    public fun show() {
        isVisible = true
    }

    public fun hide() {
        isVisible = false
    }

    public companion object {
        public val Saver: Saver<DialogState, Boolean> =
            Saver(
                save = { it.isVisible },
                restore = { DialogState(isVisible = it) },
            )
    }
}

@Composable
public fun rememberDialogState(initialVisible: Boolean = false): DialogState =
    rememberSaveable(saver = DialogState.Saver) {
        DialogState(isVisible = initialVisible)
    }
