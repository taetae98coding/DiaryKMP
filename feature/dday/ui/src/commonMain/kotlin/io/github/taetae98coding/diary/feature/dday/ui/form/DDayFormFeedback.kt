package io.github.taetae98coding.diary.feature.dday.ui.form

import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

internal suspend fun showRepeatBlank(
    state: DDayFormState,
    message: String,
) {
    coroutineScope {
        launch { state.repeatCardBringIntoViewRequester.bringIntoView() }
        state.snackbarHostState.showImmediate(message = message)
    }
}
