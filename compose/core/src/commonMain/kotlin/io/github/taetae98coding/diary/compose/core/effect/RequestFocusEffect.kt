package io.github.taetae98coding.diary.compose.core.effect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.withResumed

@Composable
public fun RequestFocusEffect(focusRequester: FocusRequester) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var isFocusRequested by retain { mutableStateOf(false) }

    LaunchedEffect(focusRequester, lifecycleOwner) {
        if (isFocusRequested) {
            return@LaunchedEffect
        }

        lifecycleOwner.lifecycle.withResumed {
            focusRequester.requestFocus()
            isFocusRequested = true
        }
    }
}
