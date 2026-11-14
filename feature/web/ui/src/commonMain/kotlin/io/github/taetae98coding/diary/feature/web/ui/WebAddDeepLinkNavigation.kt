package io.github.taetae98coding.diary.feature.web.ui

import androidx.navigation3.runtime.deeplink.DeepLinkUri
import androidx.navigation3.runtime.deeplink.UriDeepLinkMatcher
import io.github.taetae98coding.diary.core.navigation.DeepLinkNavigation
import io.github.taetae98coding.diary.core.navigation.ScreenDeepLink
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.navigation.ScreenNavigator
import io.github.taetae98coding.diary.core.navigation.toDeepLinkNavigation
import io.github.taetae98coding.diary.feature.more.api.MoreHomeNavKey
import io.github.taetae98coding.diary.feature.web.api.WebAddNavKey
import io.github.taetae98coding.diary.feature.web.api.WebHomeNavKey
import org.koin.core.annotation.Single

@Single(binds = [DeepLinkNavigation::class])
internal class WebAddDeepLinkNavigation :
    DeepLinkNavigation by UriDeepLinkMatcher(DeepLinkUri(ScreenDeepLink.WEB_ADD), WebAddNavKey.serializer())
        .toDeepLinkNavigation({ result, isLaunch -> openWebAdd(addKey = result.key.copy(isStandalone = true), isLaunch = isLaunch, listBackStack = topLevelBackStackOf(MoreHomeNavKey) + WebHomeNavKey) })

private fun ScreenNavigator.openWebAdd(
    addKey: ScreenNavKey,
    listBackStack: List<ScreenNavKey>,
    isLaunch: Boolean,
) {
    if (isLaunch) {
        backStack.clear()
        backStack.addAll(listBackStack + addKey)
    } else {
        backStack.add(addKey)
    }
}
