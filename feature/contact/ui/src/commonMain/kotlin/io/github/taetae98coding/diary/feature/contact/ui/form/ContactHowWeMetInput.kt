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
import io.github.taetae98coding.diary.feature.contact.ui.contact_how_we_met_input_label
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ContactHowWeMetInput(
    modifier: Modifier = Modifier,
    state: TextFieldState = rememberTextFieldState(),
) {
    Card(modifier = modifier) {
        ClearTextField(
            state = state,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(Res.string.contact_how_we_met_input_label)) },
            lineLimits = TextFieldLineLimits.SingleLine,
        )
    }
}

@ComponentPreview
@Composable
private fun ContactHowWeMetInputPreview() {
    DiaryTheme {
        Surface {
            ContactHowWeMetInput(state = rememberTextFieldState(initialText = "대학 동아리 신입생 환영회"))
        }
    }
}
