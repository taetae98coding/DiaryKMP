package io.github.taetae98coding.diary.feature.contact.ui.form

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.input.DiaryDescriptionInput
import io.github.taetae98coding.diary.compose.core.layout.DiaryInputColumn
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
internal fun ContactForm(
    modifier: Modifier = Modifier,
    state: ContactFormState = rememberContactAddFormState(),
) {
    DiaryInputColumn(modifier = modifier) {
        ContactNameInput(
            state = state.nameState,
            modifier = Modifier.fillMaxWidth(),
        )
        DiaryDescriptionInput(
            state = state.descriptionState,
            modifier = Modifier.fillMaxWidth(),
        )
        ContactHeightInput(
            state = state.heightState,
            modifier = Modifier.fillMaxWidth(),
        )
        ContactFootSizeInput(
            state = state.footSizeState,
            modifier = Modifier.fillMaxWidth(),
        )
        ContactBirthdayInput(
            state = state.birthdayState,
            modifier = Modifier.fillMaxWidth(),
        )
        ContactPhoneNumberInput(
            state = state.phoneNumberState,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@ScreenPreview
@Composable
private fun ContactFormPreview() {
    DiaryTheme {
        Surface {
            ContactForm()
        }
    }
}
