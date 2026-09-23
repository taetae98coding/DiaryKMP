package io.github.taetae98coding.diary.feature.contact.ui.form

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.material3.Card
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import io.github.taetae98coding.diary.compose.core.icon.AddIcon
import io.github.taetae98coding.diary.compose.core.icon.RemoveIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.textfield.ClearTextField
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.contact.ui.Res
import io.github.taetae98coding.diary.feature.contact.ui.contact_phone_number_add_button_content_description
import io.github.taetae98coding.diary.feature.contact.ui.contact_phone_number_input_label
import io.github.taetae98coding.diary.feature.contact.ui.contact_phone_number_number_input_content_description
import io.github.taetae98coding.diary.feature.contact.ui.contact_phone_number_number_input_label
import io.github.taetae98coding.diary.feature.contact.ui.contact_phone_number_remove_button_content_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ContactPhoneNumberInput(
    modifier: Modifier = Modifier,
    state: ContactPhoneNumberInputState = rememberContactPhoneNumberInputState(),
) {
    Card(modifier = modifier) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .animateContentSize(alignment = Alignment.TopStart),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(start = DiaryTheme.dimens.cardContentPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.contact_phone_number_input_label),
                    style = DiaryTheme.typography.labelLarge,
                )
                IconButton(onClick = state::add) {
                    AddIcon(contentDescription = stringResource(Res.string.contact_phone_number_add_button_content_description))
                }
            }

            state.rowList.forEachIndexed { index, row ->
                key(row) {
                    ContactPhoneNumberRow(
                        onRemove = { state.remove(row) },
                        state = row,
                        position = index + 1,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun ContactPhoneNumberRow(
    onRemove: () -> Unit,
    state: ContactPhoneNumberRowState,
    position: Int,
    modifier: Modifier = Modifier,
) {
    val numberContentDescription = stringResource(Res.string.contact_phone_number_number_input_content_description, position.toString())

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ClearTextField(
            state = state.numberState,
            modifier =
                Modifier
                    .weight(1f)
                    .semantics { contentDescription = numberContentDescription },
            label = { Text(text = stringResource(Res.string.contact_phone_number_number_input_label)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            lineLimits = TextFieldLineLimits.SingleLine,
        )
        IconButton(onClick = onRemove) {
            RemoveIcon(contentDescription = stringResource(Res.string.contact_phone_number_remove_button_content_description))
        }
    }
}

@ComponentPreview
@Composable
private fun ContactPhoneNumberInputPreview() {
    DiaryTheme {
        Surface {
            ContactPhoneNumberInput(
                state =
                    rememberContactPhoneNumberInputState(
                        initialRowList = listOf(ContactPhoneNumberRowState(initialNumber = "010-1234-5678")),
                    ),
            )
        }
    }
}
