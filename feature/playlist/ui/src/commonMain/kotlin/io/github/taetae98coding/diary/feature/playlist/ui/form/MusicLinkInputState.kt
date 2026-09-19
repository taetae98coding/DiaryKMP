package io.github.taetae98coding.diary.feature.playlist.ui.form

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester

@Stable
internal class MusicLinkInputState(
    val textFieldState: TextFieldState,
    val focusRequester: FocusRequester,
) {
    val text: CharSequence
        get() = textFieldState.text

    fun clearText() {
        textFieldState.clearText()
    }

    fun requestFocus() {
        focusRequester.requestFocus()
    }
}

@Composable
internal fun rememberMusicLinkInputState(initialText: String = ""): MusicLinkInputState {
    val textFieldState = rememberTextFieldState(initialText = initialText)
    val focusRequester = remember { FocusRequester() }

    return remember(textFieldState, focusRequester) {
        MusicLinkInputState(
            textFieldState = textFieldState,
            focusRequester = focusRequester,
        )
    }
}
