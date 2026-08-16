package io.github.taetae98coding.diary.feature.contact.ui.form

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.animation.DiaryScaleFadeVisibility
import io.github.taetae98coding.diary.compose.core.animation.DiaryValueSlide
import io.github.taetae98coding.diary.compose.core.button.ClearButton
import io.github.taetae98coding.diary.compose.core.dialog.DiaryDatePickerDialogHost
import io.github.taetae98coding.diary.compose.core.format.toDisplayText
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.contact.ContactBirthday
import io.github.taetae98coding.diary.core.model.contact.ContactBirthdayCalendar
import io.github.taetae98coding.diary.feature.contact.ui.Res
import io.github.taetae98coding.diary.feature.contact.ui.contact_birthday_clear_button_content_description
import io.github.taetae98coding.diary.feature.contact.ui.contact_birthday_input_label
import io.github.taetae98coding.diary.feature.contact.ui.contact_birthday_not_set
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock

@Composable
internal fun ContactBirthdayInput(
    modifier: Modifier = Modifier,
    state: ContactBirthdayInputState = rememberContactBirthdayInputState(),
) {
    Card(modifier = modifier) {
        Column {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.contact_birthday_input_label),
                    modifier = Modifier.weight(1F),
                    style = DiaryTheme.typography.labelLarge,
                )
                TextButton(onClick = state.pickerDialogState::show) {
                    BirthdayText(state = state)
                }
                DiaryScaleFadeVisibility(visible = state.birthday != null) {
                    ClearButton(
                        onClick = state::clear,
                        contentDescription = stringResource(Res.string.contact_birthday_clear_button_content_description),
                    )
                }
            }
            AnimatedVisibility(visible = state.birthday != null) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    ContactBirthdayCalendarSelector(
                        calendar = state.birthday?.calendar ?: ContactBirthdayCalendar.SOLAR,
                        onSelect = state::selectCalendar,
                    )
                }
            }
        }
    }

    DiaryDatePickerDialogHost(
        initialDateProvider = { state.birthday?.date ?: today() },
        onConfirm = state::selectDate,
        dialogState = state.pickerDialogState,
    )
}

@Composable
private fun BirthdayText(state: ContactBirthdayInputState) {
    DiaryValueSlide(targetState = state.birthday?.date.toEpochDayOrUnset()) { epochDay ->
        val date = epochDay.toBirthdayDateOrNull()

        if (date == null) {
            Text(text = stringResource(Res.string.contact_birthday_not_set))
        } else {
            Text(text = date.toDisplayText())
        }
    }
}

private fun today(): LocalDate =
    Clock.System
        .now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date

@ComponentPreview
@Composable
private fun ContactBirthdayInputPreview() {
    DiaryTheme {
        Surface {
            ContactBirthdayInput(
                state =
                    rememberContactBirthdayInputState(
                        initialBirthday =
                            ContactBirthday(
                                date = LocalDate(year = 1998, month = 5, day = 12),
                                calendar = ContactBirthdayCalendar.LUNAR,
                            ),
                    ),
            )
        }
    }
}
