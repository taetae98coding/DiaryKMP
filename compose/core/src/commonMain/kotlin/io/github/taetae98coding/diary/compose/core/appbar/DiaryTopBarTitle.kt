package io.github.taetae98coding.diary.compose.core.appbar

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.focus.FocusRequester

public sealed interface DiaryTopBarTitle {
    @Immutable
    public data class Text(
        val text: String,
        val subtitle: String? = null,
        val isMarquee: Boolean = false,
    ) : DiaryTopBarTitle

    @Immutable
    public data class Button(
        val text: String,
        val isExpanded: Boolean,
        val expandContentDescription: String,
        val collapseContentDescription: String,
        val onClick: () -> Unit,
    ) : DiaryTopBarTitle

    @Stable
    public class Search(
        public val state: TextFieldState,
        public val placeholder: String,
        public val focusRequester: FocusRequester,
    ) : DiaryTopBarTitle
}
