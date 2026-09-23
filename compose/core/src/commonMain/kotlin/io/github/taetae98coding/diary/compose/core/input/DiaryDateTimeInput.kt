package io.github.taetae98coding.diary.compose.core.input

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.github.taetae98coding.diary.compose.core.Res
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.diary_date_time_input_end
import io.github.taetae98coding.diary.compose.core.diary_date_time_input_start
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.stringResource

@Composable
public fun DiaryDateTimeInput(
    modifier: Modifier = Modifier,
    state: DiaryDateTimeInputState = rememberDiaryDateTimeInputState(),
) {
    val dialogState = rememberDialogState()
    var dialog by rememberSaveable(stateSaver = DiaryDateTimeInputDialog.Saver) {
        mutableStateOf(DiaryDateTimeInputDialog.StartDate)
    }

    fun showDialog(value: DiaryDateTimeInputDialog) {
        dialog = value
        dialogState.show()
    }

    Card(modifier = modifier) {
        DiaryDateTimeInputSwitchRow(
            onCheckedChange = { hasDateTime -> state.hasDateTime = hasDateTime },
            state = state,
        )
        AnimatedVisibility(visible = state.hasDateTime) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            start = DiaryTheme.dimens.cardContentPadding,
                            end = DiaryTheme.dimens.cardContentPadding,
                            bottom = DiaryDateTimeInputDefaults.PeriodBottomPadding,
                        ),
            ) {
                DiaryDateTimeInputAllDayRow(
                    onCheckedChange = state::selectAllDay,
                    state = state,
                )
                DiaryDateTimeInputRow(
                    label = stringResource(Res.string.diary_date_time_input_start),
                    onDateClick = { showDialog(DiaryDateTimeInputDialog.StartDate) },
                    onTimeClick = { showDialog(DiaryDateTimeInputDialog.StartTime) },
                    state = state,
                    dateTimeProvider = { state.start },
                )
                DiaryDateTimeInputRow(
                    label = stringResource(Res.string.diary_date_time_input_end),
                    onDateClick = { showDialog(DiaryDateTimeInputDialog.EndDate) },
                    onTimeClick = { showDialog(DiaryDateTimeInputDialog.EndTime) },
                    state = state,
                    dateTimeProvider = { state.endInclusive },
                )
            }
        }
    }

    DiaryDateTimeInputDialogHost(
        dialogProvider = { dialog },
        dialogState = dialogState,
        state = state,
    )
}

private class DiaryDateTimeInputPreviewParameter : PreviewParameterProvider<DiaryDateTimeInputValue?> {
    override val values: Sequence<DiaryDateTimeInputValue?> =
        sequenceOf(
            null,
            DiaryDateTimeInputValue.AllDay(
                dateRange = LocalDate(year = 2026, month = 7, day = 19)..LocalDate(year = 2026, month = 7, day = 20),
            ),
            DiaryDateTimeInputValue.DateTime(
                start = LocalDateTime(year = 2026, month = 7, day = 19, hour = 13, minute = 30),
                endInclusive = LocalDateTime(year = 2026, month = 7, day = 20, hour = 9, minute = 0),
            ),
        )
}

@ComponentPreview
@Composable
private fun DiaryDateTimeInputPreview(
    @PreviewParameter(DiaryDateTimeInputPreviewParameter::class) value: DiaryDateTimeInputValue?,
) {
    DiaryTheme {
        Surface {
            DiaryDateTimeInput(state = rememberDiaryDateTimeInputState(initialValue = value))
        }
    }
}
