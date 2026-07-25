package io.github.taetae98coding.diary.compose.core.input

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.Saver
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.dialog.DiaryDatePickerDialog
import io.github.taetae98coding.diary.compose.core.dialog.DiaryTimePickerDialog
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

internal enum class DiaryDateTimeInputDialog {
    StartDate,
    EndDate,
    StartTime,
    EndTime,
    ;

    companion object {
        val Saver: Saver<DiaryDateTimeInputDialog, Int> =
            Saver(
                save = { it.ordinal },
                restore = { entries[it] },
            )
    }
}

@Composable
internal fun DiaryDateTimeInputDialogHost(
    dialogProvider: () -> DiaryDateTimeInputDialog,
    dialogState: DialogState,
    state: DiaryDateTimeInputState = rememberDiaryDateTimeInputState(),
) {
    if (!dialogState.isVisible) return

    when (dialogProvider()) {
        DiaryDateTimeInputDialog.StartDate ->
            DiaryDatePickerDialog(
                initialDate = state.start.date,
                onDismissRequest = dialogState::hide,
                onConfirm = { date ->
                    state.selectStartDate(date)
                    dialogState.hide()
                },
            )

        DiaryDateTimeInputDialog.EndDate ->
            DiaryDatePickerDialog(
                initialDate = state.endInclusive.date,
                onDismissRequest = dialogState::hide,
                onConfirm = { date ->
                    state.selectEndDate(date)
                    dialogState.hide()
                },
            )

        DiaryDateTimeInputDialog.StartTime ->
            DiaryTimePickerDialog(
                initialTime = state.start.time,
                onDismissRequest = dialogState::hide,
                onConfirm = { time ->
                    state.selectStartTime(time)
                    dialogState.hide()
                },
            )

        DiaryDateTimeInputDialog.EndTime ->
            DiaryTimePickerDialog(
                initialTime = state.endInclusive.time,
                onDismissRequest = dialogState::hide,
                onConfirm = { time ->
                    state.selectEndTime(time)
                    dialogState.hide()
                },
            )
    }
}

@ComponentPreview
@Composable
private fun DiaryDateTimeInputDialogHostPreview() {
    DiaryTheme {
        DiaryDateTimeInputDialogHost(
            dialogProvider = { DiaryDateTimeInputDialog.StartDate },
            dialogState = rememberDialogState().apply { show() },
        )
    }
}
