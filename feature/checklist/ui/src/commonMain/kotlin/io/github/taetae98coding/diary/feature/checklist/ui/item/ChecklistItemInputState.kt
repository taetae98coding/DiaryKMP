package io.github.taetae98coding.diary.feature.checklist.ui.item

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester

@Stable
internal class ChecklistItemInputState(
    val textFieldState: TextFieldState,
    val focusRequester: FocusRequester,
) {
    val content: String
        get() = textFieldState.text.toString()

    val isAddable: Boolean by derivedStateOf { textFieldState.text.isNotBlank() }

    fun clear() {
        textFieldState.clearText()
    }

    fun requestFocus() {
        focusRequester.requestFocus()
    }
}

@Composable
internal fun rememberChecklistItemInputState(): ChecklistItemInputState {
    val textFieldState = rememberTextFieldState()
    val focusRequester = remember { FocusRequester() }

    return remember(textFieldState, focusRequester) {
        ChecklistItemInputState(
            textFieldState = textFieldState,
            focusRequester = focusRequester,
        )
    }
}
