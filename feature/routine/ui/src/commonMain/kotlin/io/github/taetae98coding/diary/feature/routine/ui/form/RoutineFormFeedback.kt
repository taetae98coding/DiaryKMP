package io.github.taetae98coding.diary.feature.routine.ui.form

import androidx.compose.material3.SnackbarHostState
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

internal suspend fun showRoutineAmountInvalid(
    amountState: RoutineAmountInputState,
    snackbarHostState: SnackbarHostState,
    message: String,
) {
    coroutineScope {
        launch { amountState.focus() }
        snackbarHostState.showImmediate(message = message)
    }
}
