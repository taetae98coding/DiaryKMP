package io.github.taetae98coding.diary.feature.checklist.ui

import androidx.navigation3.runtime.deeplink.DeepLinkUri
import androidx.navigation3.runtime.deeplink.UriDeepLinkMatcher
import io.github.taetae98coding.diary.core.navigation.DeepLinkNavigation
import io.github.taetae98coding.diary.core.navigation.ScreenDeepLink
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.navigation.ScreenNavigator
import io.github.taetae98coding.diary.core.navigation.toDeepLinkNavigation
import io.github.taetae98coding.diary.feature.checklist.api.ChecklistDetailNavKey
import io.github.taetae98coding.diary.feature.checklist.api.ChecklistHomeNavKey
import io.github.taetae98coding.diary.feature.more.api.MoreHomeNavKey
import org.koin.core.annotation.Single

@Single(binds = [DeepLinkNavigation::class])
internal class ChecklistDetailDeepLinkNavigation :
    DeepLinkNavigation by UriDeepLinkMatcher(DeepLinkUri(ScreenDeepLink.CHECKLIST_DETAIL), ChecklistDetailNavKey.serializer())
        .toDeepLinkNavigation({ result, isLaunch -> openChecklistDetail(detailKey = result.key.copy(isStandalone = true), isLaunch = isLaunch, listBackStack = topLevelBackStackOf(MoreHomeNavKey) + ChecklistHomeNavKey) })

private fun ScreenNavigator.openChecklistDetail(
    detailKey: ChecklistDetailNavKey,
    listBackStack: List<ScreenNavKey>,
    isLaunch: Boolean,
) {
    when {
        (backStack.lastOrNull() as? ChecklistDetailNavKey)?.id == detailKey.id -> Unit

        isLaunch -> {
            backStack.clear()
            backStack.addAll(listBackStack + detailKey)
        }

        else -> backStack.add(detailKey)
    }
}
