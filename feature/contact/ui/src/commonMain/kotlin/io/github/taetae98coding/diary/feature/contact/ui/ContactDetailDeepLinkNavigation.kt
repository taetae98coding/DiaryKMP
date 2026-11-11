package io.github.taetae98coding.diary.feature.contact.ui

import androidx.navigation3.runtime.deeplink.DeepLinkUri
import androidx.navigation3.runtime.deeplink.UriDeepLinkMatcher
import io.github.taetae98coding.diary.core.navigation.DeepLinkNavigation
import io.github.taetae98coding.diary.core.navigation.ScreenDeepLink
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.navigation.ScreenNavigator
import io.github.taetae98coding.diary.core.navigation.toDeepLinkNavigation
import io.github.taetae98coding.diary.feature.contact.api.ContactDetailNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactHomeNavKey
import io.github.taetae98coding.diary.feature.more.api.MoreHomeNavKey
import org.koin.core.annotation.Single

@Single(binds = [DeepLinkNavigation::class])
internal class ContactDetailDeepLinkNavigation :
    DeepLinkNavigation by UriDeepLinkMatcher(DeepLinkUri(ScreenDeepLink.CONTACT_DETAIL), ContactDetailNavKey.serializer())
        .toDeepLinkNavigation({ result, isLaunch -> openContactDetail(detailKey = result.key.copy(isStandalone = true), isLaunch = isLaunch, listBackStack = topLevelBackStackOf(MoreHomeNavKey) + ContactHomeNavKey) })

private fun ScreenNavigator.openContactDetail(
    detailKey: ContactDetailNavKey,
    listBackStack: List<ScreenNavKey>,
    isLaunch: Boolean,
) {
    when {
        (backStack.lastOrNull() as? ContactDetailNavKey)?.id == detailKey.id -> Unit

        isLaunch -> {
            backStack.clear()
            backStack.addAll(listBackStack + detailKey)
        }

        else -> backStack.add(detailKey)
    }
}
