package io.github.taetae98coding.diary.feature.qr.ui.add

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester

@Stable
internal class QrValueInputState(
    val textFieldState: TextFieldState,
    val focusRequester: FocusRequester,
) {
    val text: CharSequence
        get() = textFieldState.text

    fun clearText() {
        textFieldState.clearText()
    }

    fun setText(text: String) {
        textFieldState.setTextAndPlaceCursorAtEnd(text)
    }

    fun requestFocus() {
        focusRequester.requestFocus()
    }
}

@Composable
internal fun rememberQrValueInputState(): QrValueInputState {
    val textFieldState = rememberTextFieldState()
    val focusRequester = remember { FocusRequester() }

    return remember(textFieldState, focusRequester) {
        QrValueInputState(
            textFieldState = textFieldState,
            focusRequester = focusRequester,
        )
    }
}
