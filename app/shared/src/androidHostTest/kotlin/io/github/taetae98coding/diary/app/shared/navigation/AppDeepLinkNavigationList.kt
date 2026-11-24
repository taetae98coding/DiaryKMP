package io.github.taetae98coding.diary.app.shared.navigation

import io.github.taetae98coding.diary.core.navigation.DeepLinkNavigation
import io.github.taetae98coding.diary.core.navigation.ScreenDeepLinkRequest
import io.github.taetae98coding.diary.core.navigation.ScreenNavigator
import io.github.taetae98coding.diary.core.navigation.open

internal val appDeepLinkNavigationList: List<DeepLinkNavigation> by lazy {
    appTestDependencies.deepLinkNavigationList
}

internal fun ScreenNavigator.openDeepLink(
    deepLink: String,
    isLaunch: Boolean = false,
) {
    appDeepLinkNavigationList.open(request = ScreenDeepLinkRequest(deepLink = deepLink, isLaunch = isLaunch), navigator = this)
}
