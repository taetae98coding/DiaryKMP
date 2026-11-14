package io.github.taetae98coding.diary.feature.routine.ui.form

import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.placeCursorAtEnd
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import io.github.taetae98coding.diary.compose.core.textfield.keepInsertionWhile

@Stable
internal class RoutineAmountInputState(
    val textFieldState: TextFieldState,
    val focusRequester: FocusRequester,
    val bringIntoViewRequester: BringIntoViewRequester,
) {
    val amount: Int?
        get() = textFieldState.text.toString().toIntOrNull()

    val hasText: Boolean by derivedStateOf { textFieldState.text.isNotEmpty() }

    val isPositive: Boolean by derivedStateOf { (textFieldState.text.toString().toIntOrNull() ?: 0) > 0 }

    fun setAmount(amount: Int?) {
        textFieldState.setTextAndPlaceCursorAtEnd(amount?.toString().orEmpty())
    }

    fun clear() {
        textFieldState.clearText()
    }

    suspend fun focus() {
        bringIntoViewRequester.bringIntoView()
        focusRequester.requestFocus()
        textFieldState.edit { placeCursorAtEnd() }
    }
}

@Composable
internal fun rememberRoutineAmountInputState(initialAmount: Int? = null): RoutineAmountInputState {
    val textFieldState = rememberTextFieldState(initialText = initialAmount?.toString().orEmpty())
    val focusRequester = remember { FocusRequester() }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    return remember(textFieldState, focusRequester, bringIntoViewRequester) {
        RoutineAmountInputState(
            textFieldState = textFieldState,
            focusRequester = focusRequester,
            bringIntoViewRequester = bringIntoViewRequester,
        )
    }
}

// 0~9 숫자만 일곱 자리까지 남기므로 이 입력을 거친 비어 있지 않은 값은 항상 정수로 읽을 수 있다.
internal object RoutineAmountInputTransformation : InputTransformation {
    override fun TextFieldBuffer.transformInput() {
        keepInsertionWhile { text -> text.length <= MAX_DIGIT_COUNT && text.all { char -> char in DIGIT_RANGE } }
    }
}

private const val MAX_DIGIT_COUNT = 7

private val DIGIT_RANGE = '0'..'9'
