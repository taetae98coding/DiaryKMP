package io.github.taetae98coding.diary.compose.core.appbar

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.focus.FocusRequester

internal sealed interface DiaryTopBarTitle {
    @Immutable
    data class Text(
        val text: String,
        val subtitle: String?,
        val isMarquee: Boolean,
    ) : DiaryTopBarTitle

    @Immutable
    data class Button(
        val text: String,
        val isExpanded: Boolean,
        val expandContentDescription: String,
        val collapseContentDescription: String,
        val onClick: () -> Unit,
    ) : DiaryTopBarTitle

    @Stable
    class Search(
        val state: TextFieldState,
        val placeholder: String,
        val focusRequester: FocusRequester,
    ) : DiaryTopBarTitle
}
