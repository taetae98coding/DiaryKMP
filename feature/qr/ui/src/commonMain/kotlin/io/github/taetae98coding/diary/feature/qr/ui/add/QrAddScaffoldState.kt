package io.github.taetae98coding.diary.feature.qr.ui.add

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember

@Stable
internal class QrAddScaffoldState(
    val valueState: TextFieldState,
    val hostState: SnackbarHostState,
)

@Composable
internal fun rememberQrAddScaffoldState(): QrAddScaffoldState {
    val valueState = rememberTextFieldState()
    val hostState = remember { SnackbarHostState() }

    return remember(valueState, hostState) {
        QrAddScaffoldState(
            valueState = valueState,
            hostState = hostState,
        )
    }
}
