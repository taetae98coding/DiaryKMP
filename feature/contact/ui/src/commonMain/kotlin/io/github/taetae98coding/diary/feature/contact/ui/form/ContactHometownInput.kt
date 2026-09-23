package io.github.taetae98coding.diary.feature.contact.ui.form

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Card
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.textfield.ClearTextField
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.contact.ui.Res
import io.github.taetae98coding.diary.feature.contact.ui.contact_hometown_input_label
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ContactHometownInput(
    modifier: Modifier = Modifier,
    state: TextFieldState = rememberTextFieldState(),
) {
    Card(modifier = modifier) {
        ClearTextField(
            state = state,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(Res.string.contact_hometown_input_label)) },
            lineLimits = TextFieldLineLimits.SingleLine,
        )
    }
}

@ComponentPreview
@Composable
private fun ContactHometownInputPreview() {
    DiaryTheme {
        Surface {
            ContactHometownInput(state = rememberTextFieldState(initialText = "강원도 춘천시"))
        }
    }
}
