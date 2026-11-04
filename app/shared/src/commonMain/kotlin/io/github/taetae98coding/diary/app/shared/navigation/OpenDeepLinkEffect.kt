package io.github.taetae98coding.diary.app.shared.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import io.github.taetae98coding.diary.core.navigation.DeepLinkNavigation
import io.github.taetae98coding.diary.core.navigation.ScreenNavigator
import io.github.taetae98coding.diary.core.navigation.open
import kotlinx.coroutines.flow.Flow

@Composable
internal fun OpenDeepLinkEffect(
    deepLink: Flow<String>,
    deepLinkNavigationList: List<DeepLinkNavigation>,
    navigator: ScreenNavigator,
) {
    LaunchedEffect(deepLink, deepLinkNavigationList, navigator) {
        deepLink.collect { value -> deepLinkNavigationList.open(deepLink = value, navigator = navigator) }
    }
}
