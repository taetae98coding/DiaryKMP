package io.github.taetae98coding.diary.feature.routine.ui.form

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.animation.DiaryValueSlide
import io.github.taetae98coding.diary.compose.core.dialog.DiaryDatePickerDialogHost
import io.github.taetae98coding.diary.compose.core.format.toDisplayText
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.routine.ui.Res
import io.github.taetae98coding.diary.feature.routine.ui.routine_start_date_label
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun RoutineStartDateCard(
    modifier: Modifier = Modifier,
    state: RoutineFormState = rememberRoutineAddFormState(),
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.fillMaxWidth()) {
            RoutineCardRow(
                label = stringResource(Res.string.routine_start_date_label),
                modifier = Modifier.fillMaxWidth(),
            ) {
                TextButton(onClick = state.startDatePickerDialogState::show) {
                    RoutineDateText(date = state.ruleState.startDate)
                }
            }
        }
    }

    DiaryDatePickerDialogHost(
        initialDateProvider = { state.ruleState.startDate },
        onConfirm = state.ruleState::selectStartDate,
        dialogState = state.startDatePickerDialogState,
    )
}

@Composable
internal fun RoutineDateText(
    date: LocalDate,
    modifier: Modifier = Modifier,
) {
    DiaryValueSlide(
        targetState = date,
        modifier = modifier,
    ) { value ->
        Text(text = value.toDisplayText())
    }
}

@ComponentPreview
@Composable
private fun RoutineStartDateCardPreview() {
    DiaryTheme {
        Surface {
            RoutineStartDateCard(state = rememberRoutineAddFormState(initialStartDate = LocalDate(year = 2026, month = 7, day = 19)))
        }
    }
}

@ComponentPreview
@Composable
private fun RoutineDateTextPreview() {
    DiaryTheme {
        Surface {
            RoutineDateText(date = LocalDate(year = 2026, month = 7, day = 19))
        }
    }
}
