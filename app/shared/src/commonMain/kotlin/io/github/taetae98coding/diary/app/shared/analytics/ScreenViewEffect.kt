package io.github.taetae98coding.diary.app.shared.analytics

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.LifecycleStartEffect
import io.github.taetae98coding.diary.app.shared.AppState
import io.github.taetae98coding.diary.app.shared.rememberAppState
import io.github.taetae98coding.diary.logger.analytics.api.ScreenViewLog

@Composable
internal fun ScreenViewEffect(
    log: (ScreenViewLog) -> Unit,
    appState: AppState = rememberAppState(),
) {
    val screenNavKey = appState.currentScreenNavKey

    LifecycleStartEffect(screenNavKey) {
        if (screenNavKey != null) {
            log(ScreenViewLog(screenName = screenNavKey.screenName))
        }

        onStopOrDispose { }
    }
}
