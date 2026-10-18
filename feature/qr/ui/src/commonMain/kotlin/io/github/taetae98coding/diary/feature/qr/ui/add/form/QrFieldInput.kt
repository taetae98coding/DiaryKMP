package io.github.taetae98coding.diary.feature.qr.ui.add.form

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.KeyboardType
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
internal fun QrFieldInput(
    label: String,
    modifier: Modifier = Modifier,
    state: QrFieldState = rememberQrFieldState(),
    inputTransformation: InputTransformation? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    isMultiLine: Boolean = false,
) {
    OutlinedTextField(
        state = state.textFieldState,
        modifier = modifier.focusRequester(state.focusRequester),
        label = { Text(text = label) },
        inputTransformation = inputTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        lineLimits = if (isMultiLine) TextFieldLineLimits.MultiLine() else TextFieldLineLimits.SingleLine,
    )
}

@ComponentPreview
@Composable
private fun QrFieldInputPreview() {
    DiaryTheme {
        Surface {
            QrFieldInput(label = "URL")
        }
    }
}
