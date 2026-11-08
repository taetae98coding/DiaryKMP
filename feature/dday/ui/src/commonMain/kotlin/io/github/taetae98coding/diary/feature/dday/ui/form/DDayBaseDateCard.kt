package io.github.taetae98coding.diary.feature.dday.ui.form

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Card
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import io.github.taetae98coding.diary.compose.core.animation.DiaryValueSlide
import io.github.taetae98coding.diary.compose.core.dialog.DiaryDatePickerDialogHost
import io.github.taetae98coding.diary.compose.core.format.toDisplayText
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.dday.ui.Res
import io.github.taetae98coding.diary.feature.dday.ui.dday_base_date_label
import io.github.taetae98coding.diary.feature.dday.ui.dday_counts_base_day_label
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DDayBaseDateCard(
    modifier: Modifier = Modifier,
    state: DDayFormState = rememberDDayAddFormState(),
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.fillMaxWidth()) {
            DDayCardRow(
                label = stringResource(Res.string.dday_base_date_label),
                modifier = Modifier.fillMaxWidth(),
            ) {
                TextButton(onClick = state.baseDatePickerDialogState::show) {
                    DDayDateText(date = state.ruleState.baseDate)
                }
            }
            CountsBaseDayRow(
                state = state.ruleState,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    DiaryDatePickerDialogHost(
        initialDateProvider = { state.ruleState.baseDate },
        onConfirm = state.ruleState::selectBaseDate,
        dialogState = state.baseDatePickerDialogState,
    )
}

@Composable
private fun CountsBaseDayRow(
    state: DDayRuleInputState,
    modifier: Modifier = Modifier,
) {
    val countsBaseDay = state.countsBaseDay

    DDayCardRow(
        label = stringResource(Res.string.dday_counts_base_day_label),
        modifier =
            modifier.toggleable(
                value = countsBaseDay,
                role = Role.Switch,
                onValueChange = state::updateCountsBaseDay,
            ),
    ) {
        Switch(checked = countsBaseDay, onCheckedChange = null)
    }
}

@Composable
internal fun DDayDateText(
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
private fun DDayBaseDateCardPreview() {
    DiaryTheme {
        Surface {
            DDayBaseDateCard(state = rememberDDayAddFormState(initialBaseDate = LocalDate(year = 2026, month = 3, day = 20)))
        }
    }
}

@ComponentPreview
@Composable
private fun DDayDateTextPreview() {
    DiaryTheme {
        Surface {
            DDayDateText(date = LocalDate(year = 2026, month = 3, day = 20))
        }
    }
}
