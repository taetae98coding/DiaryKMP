package io.github.taetae98coding.diary.feature.routine.ui.form

import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import io.github.taetae98coding.diary.compose.core.textfield.keepInsertionWhile
import io.github.taetae98coding.diary.domain.routine.ROUTINE_DEFAULT_UNIT
import io.github.taetae98coding.diary.domain.routine.ROUTINE_UNIT_MAX_LENGTH
import io.github.taetae98coding.diary.domain.routine.isRoutineUnitLineBreak
import io.github.taetae98coding.diary.domain.routine.routineUnitLength

@Stable
internal class RoutineUnitInputState(
    val textFieldState: TextFieldState,
    val focusRequester: FocusRequester,
) {
    val text: String
        get() = textFieldState.text.toString()

    val unit: String by derivedStateOf { textFieldState.text.toString().trim() }

    val hasText: Boolean by derivedStateOf { textFieldState.text.isNotEmpty() }

    fun clear() {
        textFieldState.clearText()
    }
}

@Composable
internal fun rememberRoutineUnitInputState(initialUnit: String = ROUTINE_DEFAULT_UNIT): RoutineUnitInputState {
    val textFieldState = rememberTextFieldState(initialText = initialUnit)
    val focusRequester = remember { FocusRequester() }

    return remember(textFieldState, focusRequester) {
        RoutineUnitInputState(
            textFieldState = textFieldState,
            focusRequester = focusRequester,
        )
    }
}

internal object RoutineUnitInputTransformation : InputTransformation {
    override fun TextFieldBuffer.transformInput() {
        keepInsertionWhile { text -> text.none { char -> char.isRoutineUnitLineBreak() } && text.toString().routineUnitLength() <= ROUTINE_UNIT_MAX_LENGTH }
    }
}
