package io.github.taetae98coding.diary.feature.routine.ui.form

import androidx.compose.material3.SnackbarHostState
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal fun CoroutineScope.showRoutineAmountInvalid(
    amountState: RoutineAmountInputState,
    snackbarHostState: SnackbarHostState,
    message: String,
) {
    launch { amountState.focus() }
    launch { snackbarHostState.showImmediate(message = message) }
}
