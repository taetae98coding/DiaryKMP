package io.github.taetae98coding.diary.feature.web.ui.form

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.material3.Card
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.KeyboardType
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.textfield.ClearTextField
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.web.ui.Res
import io.github.taetae98coding.diary.feature.web.ui.web_url_input_label
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun WebUrlInput(
    modifier: Modifier = Modifier,
    state: WebUrlInputState = rememberWebUrlInputState(),
) {
    Card(modifier = modifier) {
        ClearTextField(
            state = state.textFieldState,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .focusRequester(state.focusRequester),
            label = { Text(text = stringResource(Res.string.web_url_input_label)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            lineLimits = TextFieldLineLimits.SingleLine,
        )
    }
}

@ComponentPreview
@Composable
private fun WebUrlInputPreview() {
    DiaryTheme {
        Surface {
            WebUrlInput(state = rememberWebUrlInputState(initialText = "https://developer.android.com"))
        }
    }
}
