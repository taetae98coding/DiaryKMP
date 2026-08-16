package io.github.taetae98coding.diary.feature.contact.ui.form

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.material3.Card
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.textfield.ClearTextField
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.contact.ui.Res
import io.github.taetae98coding.diary.feature.contact.ui.contact_name_input_label
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ContactNameInput(
    modifier: Modifier = Modifier,
    state: ContactNameInputState = rememberContactNameInputState(),
) {
    Card(modifier = modifier) {
        ClearTextField(
            state = state.textFieldState,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .focusRequester(state.focusRequester),
            label = { Text(text = stringResource(Res.string.contact_name_input_label)) },
            lineLimits = TextFieldLineLimits.SingleLine,
        )
    }
}

@ComponentPreview
@Composable
private fun ContactNameInputPreview() {
    DiaryTheme {
        Surface {
            ContactNameInput(state = rememberContactNameInputState(initialText = "김태영"))
        }
    }
}
