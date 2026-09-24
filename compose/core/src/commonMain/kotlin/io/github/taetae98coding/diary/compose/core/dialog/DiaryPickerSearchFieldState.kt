package io.github.taetae98coding.diary.compose.core.dialog

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester

@Stable
public class DiaryPickerSearchFieldState(
    public val textFieldState: TextFieldState,
    public val focusRequester: FocusRequester,
)

@Composable
public fun rememberDiaryPickerSearchFieldState(initialText: String = ""): DiaryPickerSearchFieldState {
    val textFieldState = rememberTextFieldState(initialText = initialText)
    val focusRequester = remember { FocusRequester() }

    return remember(textFieldState, focusRequester) {
        DiaryPickerSearchFieldState(
            textFieldState = textFieldState,
            focusRequester = focusRequester,
        )
    }
}
