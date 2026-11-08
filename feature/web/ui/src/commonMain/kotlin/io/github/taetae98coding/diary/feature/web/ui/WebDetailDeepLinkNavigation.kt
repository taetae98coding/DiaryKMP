package io.github.taetae98coding.diary.feature.web.ui

import androidx.navigation3.runtime.deeplink.DeepLinkUri
import androidx.navigation3.runtime.deeplink.UriDeepLinkMatcher
import io.github.taetae98coding.diary.core.navigation.DeepLinkNavigation
import io.github.taetae98coding.diary.core.navigation.ScreenDeepLink
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.navigation.ScreenNavigator
import io.github.taetae98coding.diary.core.navigation.toDeepLinkNavigation
import io.github.taetae98coding.diary.feature.more.api.MoreHomeNavKey
import io.github.taetae98coding.diary.feature.web.api.WebDetailNavKey
import io.github.taetae98coding.diary.feature.web.api.WebHomeNavKey
import org.koin.core.annotation.Single

@Single(binds = [DeepLinkNavigation::class])
internal class WebDetailDeepLinkNavigation :
    DeepLinkNavigation by UriDeepLinkMatcher(DeepLinkUri(ScreenDeepLink.WEB_DETAIL), WebDetailNavKey.serializer())
        .toDeepLinkNavigation({ result, isLaunch -> openWebDetail(detailKey = result.key, isLaunch = isLaunch, listBackStack = topLevelBackStackOf(MoreHomeNavKey) + WebHomeNavKey) })

// 앱 밖에서 상세를 열 때, 앱을 새로 시작했으면 목록 위에 상세를 쌓고 보던 화면이 있으면 그 위에 쌓는다. 보던 화면이 같은 상세면 쌓지 않는다.
private fun ScreenNavigator.openWebDetail(
    detailKey: ScreenNavKey,
    listBackStack: List<ScreenNavKey>,
    isLaunch: Boolean,
) {
    when {
        backStack.lastOrNull() == detailKey -> Unit

        isLaunch -> {
            backStack.clear()
            backStack.addAll(listBackStack + detailKey)
        }

        else -> backStack.add(detailKey)
    }
}
