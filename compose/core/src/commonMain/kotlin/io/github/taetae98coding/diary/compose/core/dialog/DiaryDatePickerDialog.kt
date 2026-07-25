@file:OptIn(ExperimentalMaterial3Api::class)

package io.github.taetae98coding.diary.compose.core.dialog

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.Res
import io.github.taetae98coding.diary.compose.core.date_picker_dialog_cancel
import io.github.taetae98coding.diary.compose.core.date_picker_dialog_confirm
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant

@Composable
public fun DiaryDatePickerDialog(
    initialDate: LocalDate,
    onDismissRequest: () -> Unit,
    onConfirm: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDate.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds())

    DatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        onConfirm(
                            Instant
                                .fromEpochMilliseconds(millis)
                                .toLocalDateTime(TimeZone.UTC)
                                .date,
                        )
                    }
                },
            ) {
                Text(text = stringResource(Res.string.date_picker_dialog_confirm))
            }
        },
        modifier = modifier,
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(Res.string.date_picker_dialog_cancel))
            }
        },
    ) {
        DatePicker(state = datePickerState)
    }
}

@ComponentPreview
@Composable
private fun DiaryDatePickerDialogPreview() {
    DiaryTheme {
        DiaryDatePickerDialog(
            initialDate = LocalDate(year = 2026, month = 7, day = 19),
            onDismissRequest = {},
            onConfirm = {},
        )
    }
}
