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
import io.github.taetae98coding.diary.core.model.measure.Length.Companion.centimeter
import io.github.taetae98coding.diary.feature.contact.ui.Res
import io.github.taetae98coding.diary.feature.contact.ui.contact_height_input_label
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToLong

@Composable
internal fun ContactHeightInput(
    modifier: Modifier = Modifier,
    state: TextFieldState = rememberTextFieldState(),
) {
    Card(modifier = modifier) {
        ClearTextField(
            state = state,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(Res.string.contact_height_input_label)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            lineLimits = TextFieldLineLimits.SingleLine,
            inputTransformation = ContactHeightInputTransformation,
        )
    }
}

internal object ContactHeightInputTransformation : InputTransformation {
    override fun TextFieldBuffer.transformInput() {
        val originalLength = length
        val sanitized = asCharSequence().sanitizeHeight()
        if (sanitized.contentEquals(asCharSequence())) return

        replace(0, originalLength, sanitized)
    }

    private fun CharSequence.sanitizeHeight(): String =
        buildString {
            var hasPoint = false
            var decimalDigitCount = 0

            for (char in this@sanitizeHeight) {
                when {
                    char == POINT && !hasPoint && isNotEmpty() -> {
                        hasPoint = true
                        append(char)
                    }

                    char !in DIGIT_RANGE -> Unit

                    !hasPoint && length < MAX_INTEGER_DIGIT_COUNT -> append(char)

                    hasPoint && decimalDigitCount < MAX_DECIMAL_DIGIT_COUNT -> {
                        decimalDigitCount++
                        append(char)
                    }
                }
            }
        }
}

// 입력이 0~9 숫자와 소수점 하나만 남기므로 비어 있지 않은 값은 항상 십진수로 읽을 수 있다.
internal fun TextFieldState.heightOrNull(): Length? = text.toString().toDoubleOrNull()?.centimeter

// 소수점 아래 한 자리의 센티미터는 정수 밀리미터와 같으므로 밀리미터로 반올림해 Double 문자열 변환의 오차와 지수 표기를 피한다.
internal fun Length.toHeightText(): String {
    val millimeter = inMillimeter.roundToLong()
    val integer = millimeter / MILLIMETER_PER_CENTIMETER
    val decimal = millimeter % MILLIMETER_PER_CENTIMETER

    return if (decimal == 0L) "$integer" else "$integer$POINT$decimal"
}

private const val POINT = '.'

// Char.isDigit은 전각 숫자 같은 유니코드 숫자도 받지만 toDoubleOrNull은 0~9만 읽으므로 범위로 거른다.
private val DIGIT_RANGE = '0'..'9'
private const val MAX_INTEGER_DIGIT_COUNT = 3
private const val MAX_DECIMAL_DIGIT_COUNT = 1
private const val MILLIMETER_PER_CENTIMETER = 10L

@ComponentPreview
@Composable
private fun ContactHeightInputPreview() {
    DiaryTheme {
        Surface {
            ContactHeightInput(state = rememberTextFieldState(initialText = "175.5"))
        }
    }
}
