package io.github.taetae98coding.diary.compose.core.systemscroll

import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.ui.Modifier

public expect fun Modifier.systemScroll(
    state: ScrollableState,
    onScrollToTop: suspend () -> Unit,
): Modifier
