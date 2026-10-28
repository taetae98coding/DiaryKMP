package io.github.taetae98coding.diary.app.shared

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ChromeSessionImportFailedEffect(
    snackbarHostState: SnackbarHostState,
    effect: Flow<AppChromeSessionEffect> = emptyFlow(),
) {
    val coroutineScope = rememberCoroutineScope()
    val importFailedMessage = stringResource(Res.string.chrome_session_import_failed_message)

    CollectEffect(effect) { value ->
        when (value) {
            is AppChromeSessionEffect.ImportFailed -> coroutineScope.launch { snackbarHostState.showImmediate(message = importFailedMessage) }
        }
    }
}
