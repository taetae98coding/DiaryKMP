package io.github.taetae98coding.diary.app.shared.navigation

import dev.zacsweers.metro.createGraph
import io.github.taetae98coding.diary.app.shared.di.JvmAppGraph
import io.github.taetae98coding.diary.core.navigation.DeepLinkNavigation
import io.github.taetae98coding.diary.core.navigation.ScreenDeepLinkRequest
import io.github.taetae98coding.diary.core.navigation.ScreenNavigator
import io.github.taetae98coding.diary.core.navigation.open

// 앱과 같은 그래프에서 모은 목록으로 검증해, feature가 등록을 빠뜨리면 테스트가 실패하게 한다.
internal val appDeepLinkNavigationList: List<DeepLinkNavigation> by lazy {
    createGraph<JvmAppGraph>().appDependencies.deepLinkNavigationList
}

internal fun ScreenNavigator.openDeepLink(
    deepLink: String,
    isLaunch: Boolean = false,
) {
    appDeepLinkNavigationList.open(request = ScreenDeepLinkRequest(deepLink = deepLink, isLaunch = isLaunch), navigator = this)
}
