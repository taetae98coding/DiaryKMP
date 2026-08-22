package io.github.taetae98coding.diary.feature.setting.ui.gemini

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.material3.Card
import androidx.compose.material3.IconButton
import androidx.compose.material3.SecureTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import io.github.taetae98coding.diary.compose.core.icon.VisibilityIcon
import io.github.taetae98coding.diary.compose.core.icon.VisibilityOffIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.textfield.transparentIndicator
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.setting.ui.Res
import io.github.taetae98coding.diary.feature.setting.ui.setting_gemini_api_key_hide_button_content_description
import io.github.taetae98coding.diary.feature.setting.ui.setting_gemini_api_key_label
import io.github.taetae98coding.diary.feature.setting.ui.setting_gemini_api_key_show_button_content_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SettingGeminiApiKeyInput(
    modifier: Modifier = Modifier,
    state: SettingGeminiFormState = rememberSettingGeminiFormState(),
) {
    Card(modifier = modifier) {
        SecureTextField(
            state = state.apiKeyState,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(Res.string.setting_gemini_api_key_label)) },
            trailingIcon = { VisibilityButton(state = state) },
            keyboardOptions =
                KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrectEnabled = false,
                    imeAction = ImeAction.Next,
                ),
            onKeyboardAction = { state.systemPromptFocusRequester.requestFocus() },
            textObfuscationMode = if (state.isApiKeyVisible) TextObfuscationMode.Visible else TextObfuscationMode.Hidden,
            colors = TextFieldDefaults.colors().transparentIndicator(),
        )
    }
}

@Composable
private fun VisibilityButton(
    modifier: Modifier = Modifier,
    state: SettingGeminiFormState = rememberSettingGeminiFormState(),
) {
    IconButton(
        onClick = { state.isApiKeyVisible = !state.isApiKeyVisible },
        modifier = modifier,
    ) {
        if (state.isApiKeyVisible) {
            VisibilityOffIcon(contentDescription = stringResource(Res.string.setting_gemini_api_key_hide_button_content_description))
        } else {
            VisibilityIcon(contentDescription = stringResource(Res.string.setting_gemini_api_key_show_button_content_description))
        }
    }
}

@ComponentPreview
@Composable
private fun SettingGeminiApiKeyInputPreview() {
    DiaryTheme {
        Surface {
            SettingGeminiApiKeyInput(state = rememberSettingGeminiFormState())
        }
    }
}
