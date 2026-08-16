package io.github.taetae98coding.diary.feature.contact.ui.form

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Card
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.textfield.ClearTextField
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.measure.Length
import io.github.taetae98coding.diary.core.model.measure.Length.Companion.millimeter
import io.github.taetae98coding.diary.feature.contact.ui.Res
import io.github.taetae98coding.diary.feature.contact.ui.contact_foot_size_input_label
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ContactFootSizeInput(
    modifier: Modifier = Modifier,
    state: TextFieldState = rememberTextFieldState(),
) {
    Card(modifier = modifier) {
        ClearTextField(
            state = state,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(Res.string.contact_foot_size_input_label)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            lineLimits = TextFieldLineLimits.SingleLine,
            inputTransformation = ContactFootSizeInputTransformation,
        )
    }
}

internal object ContactFootSizeInputTransformation : InputTransformation {
    override fun TextFieldBuffer.transformInput() {
        val originalLength = length
        val sanitized =
            asCharSequence()
                .filter { char -> char.isDigit() }
                .take(MAX_DIGIT_COUNT)
                .toString()
        if (sanitized.contentEquals(asCharSequence())) return

        replace(0, originalLength, sanitized)
    }
}

// 입력이 정해진 자리 수의 숫자만 남기므로 비어 있지 않은 값은 항상 정수로 읽을 수 있다.
internal fun TextFieldState.footSizeOrNull(): Length? = text.toString().toIntOrNull()?.millimeter

private const val MAX_DIGIT_COUNT = 3

@ComponentPreview
@Composable
private fun ContactFootSizeInputPreview() {
    DiaryTheme {
        Surface {
            ContactFootSizeInput(state = rememberTextFieldState(initialText = "250"))
        }
    }
}
