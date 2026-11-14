package io.github.taetae98coding.diary.feature.contact.ui.form

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.material3.Card
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.BooleanPreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.textfield.ClearTextField
import io.github.taetae98coding.diary.compose.core.textfield.keepInsertionWhile
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.contact.ContactBirthday
import io.github.taetae98coding.diary.core.model.contact.ContactBirthdayCalendar
import io.github.taetae98coding.diary.feature.contact.ui.Res
import io.github.taetae98coding.diary.feature.contact.ui.contact_birth_year_from_birthday
import io.github.taetae98coding.diary.feature.contact.ui.contact_birth_year_input_label
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ContactBirthYearInput(
    modifier: Modifier = Modifier,
    state: ContactBirthdayInputState = rememberContactBirthdayInputState(),
) {
    Card(modifier = modifier) {
        Column {
            ClearTextField(
                state = state.birthYearState,
                modifier = Modifier.fillMaxWidth(),
                enabled = state.birthday == null,
                label = { Text(text = stringResource(Res.string.contact_birth_year_input_label)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                lineLimits = TextFieldLineLimits.SingleLine,
                inputTransformation = ContactBirthYearInputTransformation,
            )
            AnimatedVisibility(visible = state.birthday != null) {
                Text(
                    text = stringResource(Res.string.contact_birth_year_from_birthday),
                    modifier =
                        Modifier.padding(
                            start = DiaryTheme.dimens.cardContentPadding,
                            top = DiaryTheme.dimens.cardLineSpacing,
                            end = DiaryTheme.dimens.cardContentPadding,
                            bottom = DiaryTheme.dimens.cardContentPadding,
                        ),
                    style = DiaryTheme.typography.bodySmall,
                )
            }
        }
    }
}

internal object ContactBirthYearInputTransformation : InputTransformation {
    override fun TextFieldBuffer.transformInput() {
        keepInsertionWhile { text -> text.length <= MAX_DIGIT_COUNT && text.all { char -> char in DIGIT_RANGE } }
    }
}

private const val MAX_DIGIT_COUNT = 4

private val DIGIT_RANGE = '0'..'9'

@ComponentPreview
@Composable
private fun ContactBirthYearInputPreview(
    @PreviewParameter(BooleanPreviewParameter::class) hasBirthday: Boolean,
) {
    DiaryTheme {
        Surface {
            ContactBirthYearInput(
                state =
                    rememberContactBirthdayInputState(
                        initialBirthday =
                            ContactBirthday(
                                date = LocalDate(year = 1998, month = 5, day = 12),
                                calendar = ContactBirthdayCalendar.SOLAR,
                            ).takeIf { hasBirthday },
                        initialBirthYear = 1998,
                    ),
            )
        }
    }
}
