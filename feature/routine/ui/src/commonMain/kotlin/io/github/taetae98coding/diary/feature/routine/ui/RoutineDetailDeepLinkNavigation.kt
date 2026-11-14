package io.github.taetae98coding.diary.feature.routine.ui

import androidx.navigation3.runtime.deeplink.DeepLinkUri
import androidx.navigation3.runtime.deeplink.UriDeepLinkMatcher
import io.github.taetae98coding.diary.core.navigation.DeepLinkNavigation
import io.github.taetae98coding.diary.core.navigation.ScreenDeepLink
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.navigation.ScreenNavigator
import io.github.taetae98coding.diary.core.navigation.toDeepLinkNavigation
import io.github.taetae98coding.diary.feature.routine.api.RoutineDetailNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineHomeNavKey
import org.koin.core.annotation.Single

@Single(binds = [DeepLinkNavigation::class])
internal class RoutineDetailDeepLinkNavigation :
    DeepLinkNavigation by UriDeepLinkMatcher(DeepLinkUri(ScreenDeepLink.ROUTINE_DETAIL), RoutineDetailNavKey.serializer())
        .toDeepLinkNavigation({ result, isLaunch -> openRoutineDetail(detailKey = result.key.copy(isStandalone = true), isLaunch = isLaunch, listBackStack = topLevelBackStackOf(RoutineHomeNavKey)) })

private fun ScreenNavigator.openRoutineDetail(
    detailKey: RoutineDetailNavKey,
    listBackStack: List<ScreenNavKey>,
    isLaunch: Boolean,
) {
    when {
        (backStack.lastOrNull() as? RoutineDetailNavKey)?.id == detailKey.id -> Unit

        isLaunch -> {
            backStack.clear()
            backStack.addAll(listBackStack + detailKey)
        }

        else -> backStack.add(detailKey)
    }
}
