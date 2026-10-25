package io.github.taetae98coding.diary.feature.checklist.ui.detail

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.checklist.ui.Res
import io.github.taetae98coding.diary.feature.checklist.ui.checklist_detail_reset_dialog_cancel
import io.github.taetae98coding.diary.feature.checklist.ui.checklist_detail_reset_dialog_confirm
import io.github.taetae98coding.diary.feature.checklist.ui.checklist_detail_reset_dialog_text
import io.github.taetae98coding.diary.feature.checklist.ui.checklist_detail_reset_dialog_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ChecklistDetailResetDialogHost(
    onEvent: (ChecklistDetailScaffoldEvent) -> Unit,
    state: DialogState = rememberDialogState(),
) {
    if (!state.isVisible) return

    ChecklistDetailResetDialog(
        onEvent = { event ->
            state.hide()
            onEvent(event)
        },
        onDismissRequest = state::hide,
    )
}

@Composable
internal fun ChecklistDetailResetDialog(
    onEvent: (ChecklistDetailScaffoldEvent) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = { onEvent(ChecklistDetailScaffoldEvent.ConfirmReset) }) {
                Text(text = stringResource(Res.string.checklist_detail_reset_dialog_confirm))
            }
        },
        modifier = modifier,
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(Res.string.checklist_detail_reset_dialog_cancel))
            }
        },
        title = { Text(text = stringResource(Res.string.checklist_detail_reset_dialog_title)) },
        text = { Text(text = stringResource(Res.string.checklist_detail_reset_dialog_text)) },
    )
}

@ComponentPreview
@Composable
private fun ChecklistDetailResetDialogPreview() {
    DiaryTheme {
        ChecklistDetailResetDialog(
            onEvent = {},
            onDismissRequest = {},
        )
    }
}

@ComponentPreview
@Composable
private fun ChecklistDetailResetDialogHostPreview() {
    DiaryTheme {
        ChecklistDetailResetDialogHost(
            onEvent = {},
            state = rememberDialogState(initialVisible = true),
        )
    }
}
