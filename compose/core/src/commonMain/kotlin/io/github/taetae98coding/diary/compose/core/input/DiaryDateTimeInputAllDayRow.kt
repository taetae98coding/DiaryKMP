package io.github.taetae98coding.diary.compose.core.input

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import io.github.taetae98coding.diary.compose.core.Res
import io.github.taetae98coding.diary.compose.core.diary_date_time_input_all_day
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DiaryDateTimeInputAllDayRow(
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    state: DiaryDateTimeInputState = rememberDiaryDateTimeInputState(),
) {
    val isAllDay = state.isAllDay
    val enabled = state.hasDateTime

    Row(
        modifier =
            modifier
                .clip(CircleShape)
                .toggleable(
                    value = isAllDay,
                    enabled = enabled,
                    role = Role.Checkbox,
                    onValueChange = onCheckedChange,
                ).minimumInteractiveComponentSize()
                .padding(horizontal = DiaryDateTimeInputDefaults.AllDayRowHorizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(DiaryDateTimeInputDefaults.AllDayRowSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = isAllDay,
            onCheckedChange = null,
            enabled = enabled,
        )
        Text(text = stringResource(Res.string.diary_date_time_input_all_day))
    }
}

@ComponentPreview
@Composable
private fun DiaryDateTimeInputAllDayRowPreview() {
    DiaryTheme {
        Surface {
            DiaryDateTimeInputAllDayRow(
                onCheckedChange = {},
                state = rememberDiaryDateTimeInputState(initialValue = DiaryDateTimeInputValue.AllDay(dateRange = PreviewDate..PreviewDate)),
            )
        }
    }
}

private val PreviewDate = LocalDate(year = 2026, month = 7, day = 19)
