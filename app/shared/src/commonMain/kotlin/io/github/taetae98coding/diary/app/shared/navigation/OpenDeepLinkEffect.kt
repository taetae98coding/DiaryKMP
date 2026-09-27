package io.github.taetae98coding.diary.app.shared.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import io.github.taetae98coding.diary.app.shared.AppState
import kotlinx.coroutines.flow.Flow

@Composable
internal fun OpenDeepLinkEffect(
    deepLink: Flow<String>,
    appState: AppState,
) {
    LaunchedEffect(deepLink, appState) {
        deepLink.collect { value -> appState.openDeepLink(deepLink = value) }
    }
}
