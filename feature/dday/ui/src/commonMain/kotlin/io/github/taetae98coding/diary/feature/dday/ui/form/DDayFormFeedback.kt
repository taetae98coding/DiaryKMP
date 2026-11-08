package io.github.taetae98coding.diary.feature.dday.ui.form

import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal fun CoroutineScope.showRepeatBlank(
    state: DDayFormState,
    message: String,
) {
    launch { state.repeatCardBringIntoViewRequester.bringIntoView() }
    launch { state.snackbarHostState.showImmediate(message = message) }
}
