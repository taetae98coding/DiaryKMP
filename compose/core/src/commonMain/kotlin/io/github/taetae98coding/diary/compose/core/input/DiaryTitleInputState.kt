package io.github.taetae98coding.diary.compose.core.input

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester

@Stable
public class DiaryTitleInputState(
    internal val textFieldState: TextFieldState,
    internal val focusRequester: FocusRequester,
) {
    public val text: CharSequence
        get() = textFieldState.text

    public fun clearText() {
        textFieldState.clearText()
    }

    public fun setText(text: CharSequence) {
        textFieldState.setTextAndPlaceCursorAtEnd(text.toString())
    }

    public fun requestFocus() {
        focusRequester.requestFocus()
    }
}

@Composable
public fun rememberDiaryTitleInputState(initialText: String = ""): DiaryTitleInputState {
    val textFieldState = rememberTextFieldState(initialText = initialText)
    val focusRequester = remember { FocusRequester() }

    return remember(textFieldState, focusRequester) {
        DiaryTitleInputState(
            textFieldState = textFieldState,
            focusRequester = focusRequester,
        )
    }
}
