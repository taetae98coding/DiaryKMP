@file:OptIn(ExperimentalMaterial3Api::class)

package io.github.taetae98coding.diary.compose.core.dialog

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.TimePickerDialogDefaults
import androidx.compose.material3.TimePickerDisplayMode
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.Res
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.core.time_picker_dialog_cancel
import io.github.taetae98coding.diary.compose.core.time_picker_dialog_confirm
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.stringResource

@Composable
public fun DiaryTimePickerDialog(
    initialTime: LocalTime,
    onDismissRequest: () -> Unit,
    onConfirm: (LocalTime) -> Unit,
    modifier: Modifier = Modifier,
) {
    val timePickerState =
        rememberTimePickerState(
            initialHour = initialTime.hour,
            initialMinute = initialTime.minute,
        )

    TimePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = { onConfirm(LocalTime(hour = timePickerState.hour, minute = timePickerState.minute)) }) {
                Text(text = stringResource(Res.string.time_picker_dialog_confirm))
            }
        },
        title = { TimePickerDialogDefaults.Title(displayMode = TimePickerDisplayMode.Picker) },
        modifier = modifier,
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(Res.string.time_picker_dialog_cancel))
            }
        },
    ) {
        TimePicker(state = timePickerState)
    }
}

@ComponentPreview
@Composable
private fun DiaryTimePickerDialogPreview() {
    DiaryTheme {
        DiaryTimePickerDialog(
            initialTime = LocalTime(hour = 13, minute = 30),
            onDismissRequest = {},
            onConfirm = {},
        )
    }
}
