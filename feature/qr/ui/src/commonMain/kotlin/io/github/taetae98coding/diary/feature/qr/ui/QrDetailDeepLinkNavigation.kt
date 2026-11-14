package io.github.taetae98coding.diary.feature.qr.ui

import androidx.navigation3.runtime.deeplink.DeepLinkUri
import androidx.navigation3.runtime.deeplink.UriDeepLinkMatcher
import io.github.taetae98coding.diary.core.navigation.DeepLinkNavigation
import io.github.taetae98coding.diary.core.navigation.ScreenDeepLink
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.navigation.ScreenNavigator
import io.github.taetae98coding.diary.core.navigation.toDeepLinkNavigation
import io.github.taetae98coding.diary.feature.more.api.MoreHomeNavKey
import io.github.taetae98coding.diary.feature.qr.api.QrDetailNavKey
import io.github.taetae98coding.diary.feature.qr.api.QrHomeNavKey
import org.koin.core.annotation.Single

@Single(binds = [DeepLinkNavigation::class])
internal class QrDetailDeepLinkNavigation :
    DeepLinkNavigation by UriDeepLinkMatcher(DeepLinkUri(ScreenDeepLink.QR_DETAIL), QrDetailNavKey.serializer())
        .toDeepLinkNavigation({ result, isLaunch -> openQrDetail(detailKey = result.key.copy(isStandalone = true), isLaunch = isLaunch, listBackStack = topLevelBackStackOf(MoreHomeNavKey) + QrHomeNavKey) })

private fun ScreenNavigator.openQrDetail(
    detailKey: QrDetailNavKey,
    listBackStack: List<ScreenNavKey>,
    isLaunch: Boolean,
) {
    when {
        (backStack.lastOrNull() as? QrDetailNavKey)?.id == detailKey.id -> Unit

        isLaunch -> {
            backStack.clear()
            backStack.addAll(listBackStack + detailKey)
        }

        else -> backStack.add(detailKey)
    }
}
