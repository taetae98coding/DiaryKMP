package io.github.taetae98coding.diary.compose.core.textfield

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Surface
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextFieldLabelScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import io.github.taetae98coding.diary.compose.core.Res
import io.github.taetae98coding.diary.compose.core.animation.DiaryScaleFadeVisibility
import io.github.taetae98coding.diary.compose.core.button.ClearButton
import io.github.taetae98coding.diary.compose.core.clear_text_field_button_content_description
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import org.jetbrains.compose.resources.stringResource

@Composable
public fun ClearTextField(
    modifier: Modifier = Modifier,
    state: TextFieldState = rememberTextFieldState(),
    focusRequester: FocusRequester = remember { FocusRequester() },
    label: @Composable (TextFieldLabelScope.() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    inputTransformation: InputTransformation? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onKeyboardAction: KeyboardActionHandler? = null,
    lineLimits: TextFieldLineLimits = TextFieldLineLimits.Default,
    colors: TextFieldColors = TextFieldDefaults.colors().transparentIndicator(),
    clearButtonContentDescription: String = stringResource(Res.string.clear_text_field_button_content_description),
) {
    val hasText by remember(state) { derivedStateOf { state.text.isNotEmpty() } }

    TextField(
        state = state,
        modifier = modifier.focusRequester(focusRequester),
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = {
            DiaryScaleFadeVisibility(visible = hasText) {
                ClearButton(
                    onClick = {
                        state.clearText()
                        focusRequester.requestFocus()
                    },
                    contentDescription = clearButtonContentDescription,
                )
            }
        },
        inputTransformation = inputTransformation,
        keyboardOptions = keyboardOptions,
        onKeyboardAction = onKeyboardAction,
        lineLimits = lineLimits,
        colors = colors,
    )
}

@ComponentPreview
@Composable
private fun ClearTextFieldPreview() {
    DiaryTheme {
        Surface {
            ClearTextField(state = rememberTextFieldState(initialText = "ClearTextField"))
        }
    }
}
