package io.github.taetae98coding.diary.compose.core.input

import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.focus.FocusRequester

@Stable
public class DiaryDescriptionInputState internal constructor(
    internal val textFieldState: TextFieldState,
    internal val swipeState: AnchoredDraggableState<DiaryDescriptionInputPage>,
    internal val focusRequester: FocusRequester,
) {
    public val text: CharSequence
        get() = textFieldState.text

    public val focusTarget: FocusRequester
        get() =
            if (swipeState.currentValue == DiaryDescriptionInputPage.Input) {
                focusRequester
            } else {
                FocusRequester.Default
            }

    public fun clearText() {
        textFieldState.clearText()
    }

    public fun setText(text: CharSequence) {
        textFieldState.setTextAndPlaceCursorAtEnd(text.toString())
    }
}

internal enum class DiaryDescriptionInputPage {
    Input,
    Preview,
}

@Composable
public fun rememberDiaryDescriptionInputState(initialText: String = ""): DiaryDescriptionInputState {
    val textFieldState = rememberTextFieldState(initialText = initialText)
    val swipeState =
        rememberSaveable(
            saver =
                Saver(
                    save = { it.currentValue.name },
                    restore = { AnchoredDraggableState(initialValue = DiaryDescriptionInputPage.valueOf(it)) },
                ),
        ) {
            AnchoredDraggableState(initialValue = initialDiaryDescriptionInputPage(initialText))
        }

    val focusRequester = remember { FocusRequester() }

    return remember(textFieldState, swipeState, focusRequester) {
        DiaryDescriptionInputState(
            textFieldState = textFieldState,
            swipeState = swipeState,
            focusRequester = focusRequester,
        )
    }
}

private fun initialDiaryDescriptionInputPage(initialText: String): DiaryDescriptionInputPage =
    if (initialText.isBlank()) {
        DiaryDescriptionInputPage.Input
    } else {
        DiaryDescriptionInputPage.Preview
    }
