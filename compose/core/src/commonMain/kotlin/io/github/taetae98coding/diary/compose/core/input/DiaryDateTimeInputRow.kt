package io.github.taetae98coding.diary.compose.core.input

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.format.toDisplayText
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import kotlinx.datetime.LocalDateTime

@Composable
internal fun DiaryDateTimeInputRow(
    label: String,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit,
    modifier: Modifier = Modifier,
    state: DiaryDateTimeInputState = rememberDiaryDateTimeInputState(),
    dateTimeProvider: () -> LocalDateTime = { state.start },
) {
    val dateTime = dateTimeProvider()
    val isAllDay = state.isAllDay
    val enabled = state.hasDateTime

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1F),
        )
        TextButton(
            onClick = onDateClick,
            enabled = enabled,
        ) {
            AnimatedValueText(value = dateTime.date, text = { it.toDisplayText() })
        }
        AnimatedVisibility(visible = !isAllDay) {
            TextButton(
                onClick = onTimeClick,
                enabled = enabled && !isAllDay,
            ) {
                AnimatedValueText(value = dateTime.time, text = { it.toDisplayText() })
            }
        }
    }
}

@ComponentPreview
@Composable
private fun DiaryDateTimeInputRowPreview() {
    DiaryTheme {
        Surface {
            DiaryDateTimeInputRow(
                label = "시작",
                onDateClick = {},
                onTimeClick = {},
                state =
                    rememberDiaryDateTimeInputState(
                        initialValue =
                            DiaryDateTimeInputValue.DateTime(
                                start = LocalDateTime(year = 2026, month = 7, day = 19, hour = 13, minute = 30),
                                endInclusive = LocalDateTime(year = 2026, month = 7, day = 20, hour = 9, minute = 0),
                            ),
                    ),
            )
        }
    }
}
