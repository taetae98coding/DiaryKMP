package io.github.taetae98coding.diary.compose.core.input

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.Res
import io.github.taetae98coding.diary.compose.core.diary_date_time_input_label
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DiaryDateTimeInputSwitchRow(
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    state: DiaryDateTimeInputState = rememberDiaryDateTimeInputState(),
) {
    val hasDateTime = state.hasDateTime

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .toggleable(
                    value = hasDateTime,
                    role = Role.Switch,
                    onValueChange = onCheckedChange,
                ).minimumInteractiveComponentSize()
                .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(Res.string.diary_date_time_input_label),
            modifier = Modifier.weight(1F),
        )
        Switch(checked = hasDateTime, onCheckedChange = null)
    }
}

@ComponentPreview
@Composable
private fun DiaryDateTimeInputSwitchRowPreview() {
    DiaryTheme {
        Surface {
            DiaryDateTimeInputSwitchRow(
                onCheckedChange = {},
                state = rememberDiaryDateTimeInputState(initialValue = DiaryDateTimeInputValue.AllDay(dateRange = PreviewDate..PreviewDate)),
            )
        }
    }
}

private val PreviewDate = LocalDate(year = 2026, month = 7, day = 19)
