package io.github.taetae98coding.diary.compose.core.input

import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.github.taetae98coding.diary.compose.core.text.takeLastGrapheme

@Stable
public class DiaryEmojiInputState internal constructor(
    initialText: String,
) {
    public var text: String by mutableStateOf(initialText.takeLastGrapheme().toString())
        private set

    public fun clearText() {
        text = ""
    }

    public fun setText(text: CharSequence) {
        this.text = text.takeLastGrapheme().toString()
    }

    public companion object {
        internal val Saver =
            Saver<DiaryEmojiInputState, String>(
                save = { it.text },
                restore = { DiaryEmojiInputState(initialText = it) },
            )
    }
}

internal object SingleGraphemeInputTransformation : InputTransformation {
    override fun TextFieldBuffer.transformInput() {
        val graphemeLength = asCharSequence().takeLastGrapheme().length
        if (graphemeLength < length) {
            replace(0, length - graphemeLength, "")
        }
    }
}

@Composable
public fun rememberDiaryEmojiInputState(initialText: String = ""): DiaryEmojiInputState =
    rememberSaveable(saver = DiaryEmojiInputState.Saver) {
        DiaryEmojiInputState(initialText = initialText)
    }
