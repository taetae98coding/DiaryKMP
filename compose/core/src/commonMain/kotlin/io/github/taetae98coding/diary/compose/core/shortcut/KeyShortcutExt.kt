package io.github.taetae98coding.diary.compose.core.shortcut

import androidx.compose.foundation.focusable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import kotlinx.coroutines.flow.filter

@Composable
public fun Modifier.keyShortcut(
    isEnableProvider: () -> Boolean = { true },
    onKeyEvent: (KeyEvent) -> Boolean,
): Modifier {
    val focusRequester = remember { FocusRequester() }
    val latestIsEnableProvider by rememberUpdatedState(isEnableProvider)

    LaunchedEffect(focusRequester) {
        snapshotFlow { latestIsEnableProvider() }
            .filter { isEnable -> isEnable }
            .collect { focusRequester.requestFocus() }
    }

    return focusRequester(focusRequester)
        .focusable()
        .onPreviewKeyEvent { keyEvent -> latestIsEnableProvider() && onKeyEvent(keyEvent) }
}

public fun KeyEvent.isSubmitShortcut(): Boolean = type == KeyEventType.KeyDown && isMetaPressed && key == Key.Enter

public fun KeyEvent.isAddShortcut(): Boolean = type == KeyEventType.KeyDown && isMetaPressed && key == Key.A

public fun Modifier.submitShortcut(
    isEnabledProvider: () -> Boolean = { true },
    onSubmit: () -> Unit,
): Modifier =
    onPreviewKeyEvent { keyEvent ->
        if (isEnabledProvider() && keyEvent.isSubmitShortcut()) {
            onSubmit()
            true
        } else {
            false
        }
    }
