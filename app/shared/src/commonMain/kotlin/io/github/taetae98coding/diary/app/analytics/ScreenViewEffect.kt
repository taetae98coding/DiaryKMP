package io.github.taetae98coding.diary.app.analytics

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.LifecycleStartEffect
import io.github.taetae98coding.diary.app.AppState
import io.github.taetae98coding.diary.app.rememberAppState
import io.github.taetae98coding.diary.logger.analytics.api.ScreenViewLog

@Composable
internal fun ScreenViewEffect(
    log: (ScreenViewLog) -> Unit,
    appState: AppState = rememberAppState(),
) {
    val screenNavKey = appState.currentScreenNavKey

    // 화면이 바뀔 때뿐 아니라 백그라운드에서 돌아오거나 화면이 재생성될 때도 다시 남겨야 하므로 시작 시점에 맞춘다.
    LifecycleStartEffect(screenNavKey) {
        if (screenNavKey != null) {
            log(ScreenViewLog(screenName = screenNavKey.screenName))
        }

        onStopOrDispose { }
    }
}
