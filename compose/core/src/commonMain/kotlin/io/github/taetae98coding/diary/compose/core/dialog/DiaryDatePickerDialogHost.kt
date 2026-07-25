package io.github.taetae98coding.diary.compose.core.dialog

import androidx.compose.runtime.Composable
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import kotlinx.datetime.LocalDate

@Composable
public fun DiaryDatePickerDialogHost(
    initialDateProvider: () -> LocalDate,
    onConfirm: (LocalDate) -> Unit,
    dialogState: DialogState = rememberDialogState(),
) {
    if (!dialogState.isVisible) return

    DiaryDatePickerDialog(
        initialDate = initialDateProvider(),
        onDismissRequest = dialogState::hide,
        onConfirm = { date ->
            dialogState.hide()
            onConfirm(date)
        },
    )
}

@ComponentPreview
@Composable
private fun DiaryDatePickerDialogHostPreview() {
    DiaryTheme {
        DiaryDatePickerDialogHost(
            initialDateProvider = { LocalDate(year = 2026, month = 7, day = 19) },
            onConfirm = {},
            dialogState = rememberDialogState().apply { show() },
        )
    }
}
