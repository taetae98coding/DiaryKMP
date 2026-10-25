package io.github.taetae98coding.diary.feature.setting.ui.gemini.form

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.textfield.transparentIndicator
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.setting.ui.Res
import io.github.taetae98coding.diary.feature.setting.ui.setting_gemini_system_prompt_label
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SettingGeminiSystemPromptInput(
    modifier: Modifier = Modifier,
    state: SettingGeminiFormState = rememberSettingGeminiFormState(),
) {
    Card(modifier = modifier) {
        TextField(
            state = state.systemPromptState,
            modifier =
                Modifier
                    .fillMaxSize()
                    .focusRequester(state.systemPromptFocusRequester),
            label = { Text(text = stringResource(Res.string.setting_gemini_system_prompt_label)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.None),
            colors = TextFieldDefaults.colors().transparentIndicator(),
        )
    }
}

@ComponentPreview
@Composable
private fun SettingGeminiSystemPromptInputPreview() {
    DiaryTheme {
        Surface {
            SettingGeminiSystemPromptInput()
        }
    }
}
