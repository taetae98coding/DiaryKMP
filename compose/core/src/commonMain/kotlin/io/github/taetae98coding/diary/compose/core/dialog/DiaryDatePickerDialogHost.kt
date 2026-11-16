package io.github.taetae98coding.diary.compose.core.dialog

import androidx.compose.runtime.Composable
import io.github.taetae98coding.diary.compose.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.theme.preview.ComponentPreview
import kotlinx.datetime.LocalDate

@Composable
public fun DiaryDatePickerDialogHost(
    initialDateProvider: () -> LocalDate,
    onConfirm: (LocalDate) -> Unit,
    dialogState: DialogState = rememberDialogState(),
    minDateProvider: () -> LocalDate? = { null },
) {
    if (!dialogState.isVisible) return

    DiaryDatePickerDialog(
        initialDate = initialDateProvider(),
        onDismissRequest = dialogState::hide,
        onConfirm = { date ->
            dialogState.hide()
            onConfirm(date)
        },
        minDate = minDateProvider(),
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
