package io.github.taetae98coding.diary.feature.dday.ui

import androidx.navigation3.runtime.deeplink.DeepLinkUri
import androidx.navigation3.runtime.deeplink.UriDeepLinkMatcher
import io.github.taetae98coding.diary.core.navigation.DeepLinkNavigation
import io.github.taetae98coding.diary.core.navigation.ScreenDeepLink
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.navigation.ScreenNavigator
import io.github.taetae98coding.diary.core.navigation.toDeepLinkNavigation
import io.github.taetae98coding.diary.feature.dday.api.DDayDetailNavKey
import io.github.taetae98coding.diary.feature.dday.api.DDayHomeNavKey
import io.github.taetae98coding.diary.feature.more.api.MoreHomeNavKey
import org.koin.core.annotation.Single

@Single(binds = [DeepLinkNavigation::class])
internal class DDayDetailDeepLinkNavigation :
    DeepLinkNavigation by UriDeepLinkMatcher(DeepLinkUri(ScreenDeepLink.DDAY_DETAIL), DDayDetailNavKey.serializer())
        .toDeepLinkNavigation({ result, isLaunch -> openDDayDetail(detailKey = result.key.copy(isStandalone = true), isLaunch = isLaunch, listBackStack = topLevelBackStackOf(MoreHomeNavKey) + DDayHomeNavKey) })

private fun ScreenNavigator.openDDayDetail(
    detailKey: DDayDetailNavKey,
    listBackStack: List<ScreenNavKey>,
    isLaunch: Boolean,
) {
    when {
        (backStack.lastOrNull() as? DDayDetailNavKey)?.id == detailKey.id -> Unit

        isLaunch -> {
            backStack.clear()
            backStack.addAll(listBackStack + detailKey)
        }

        else -> backStack.add(detailKey)
    }
}
