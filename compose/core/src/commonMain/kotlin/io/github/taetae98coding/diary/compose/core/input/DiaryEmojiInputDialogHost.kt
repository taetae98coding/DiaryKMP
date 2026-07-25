package io.github.taetae98coding.diary.compose.core.input

import androidx.compose.runtime.Composable
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
internal fun DiaryEmojiInputDialogHost(
    dialogState: DialogState,
    state: DiaryEmojiInputState = rememberDiaryEmojiInputState(),
) {
    if (!dialogState.isVisible) return

    DiaryEmojiInputDialog(
        onDismissRequest = dialogState::hide,
        onConfirm = { text ->
            dialogState.hide()
            state.setText(text = text)
        },
        initialText = state.text,
    )
}

@ComponentPreview
@Composable
private fun DiaryEmojiInputDialogHostPreview() {
    DiaryTheme {
        DiaryEmojiInputDialogHost(
            dialogState = rememberDialogState().apply { show() },
            state = rememberDiaryEmojiInputState(initialText = "🏃"),
        )
    }
}
