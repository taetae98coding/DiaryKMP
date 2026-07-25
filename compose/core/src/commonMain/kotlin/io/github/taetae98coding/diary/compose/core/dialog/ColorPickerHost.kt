package io.github.taetae98coding.diary.compose.core.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
public fun ColorPickerHost(
    initialColorProvider: () -> Color,
    onConfirm: (Color) -> Unit,
    modifier: Modifier = Modifier,
    dialogState: DialogState = rememberDialogState(),
) {
    if (!dialogState.isVisible) return

    val pickerState = rememberColorPickerState(initialColor = initialColorProvider())

    ColorPickerDialog(
        state = pickerState,
        onDismissRequest = dialogState::hide,
        onConfirm = { color ->
            onConfirm(color)
            dialogState.hide()
        },
        modifier = modifier,
    )
}

@ComponentPreview
@Composable
private fun ColorPickerHostPreview() {
    DiaryTheme {
        ColorPickerHost(
            initialColorProvider = { Color(color = 0xFF3A7BD5.toInt()) },
            onConfirm = {},
            dialogState = rememberDialogState().apply { show() },
        )
    }
}
