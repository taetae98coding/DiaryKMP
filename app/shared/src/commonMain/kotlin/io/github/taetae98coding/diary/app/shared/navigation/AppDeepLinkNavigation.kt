package io.github.taetae98coding.diary.app.shared.navigation

import androidx.navigation3.runtime.deeplink.DeepLinkUri
import androidx.navigation3.runtime.deeplink.UriDeepLinkMatcher
import androidx.navigation3.runtime.deeplink.withBackStack
import io.github.taetae98coding.diary.core.navigation.DeepLinkNavigation
import io.github.taetae98coding.diary.core.navigation.ScreenDeepLink
import io.github.taetae98coding.diary.core.navigation.toDeepLinkNavigation
import io.github.taetae98coding.diary.feature.file.api.FileHomeNavKey
import io.github.taetae98coding.diary.feature.file.api.openFileHome
import io.github.taetae98coding.diary.feature.timetable.api.CalendarTimetableNavKey
import io.github.taetae98coding.diary.feature.timetable.api.openTimetable

internal val appDeepLinkNavigationList: List<DeepLinkNavigation> =
    listOf(
        UriDeepLinkMatcher(DeepLinkUri(ScreenDeepLink.FILE_HOME), FileHomeNavKey.serializer())
            .withBackStack { result -> fileHomeParentNavKeyList + result.key }
            .toDeepLinkNavigation { result -> openFileHome(result.backStack) },
        UriDeepLinkMatcher(DeepLinkUri(ScreenDeepLink.TIMETABLE), CalendarTimetableNavKey.serializer())
            .toDeepLinkNavigation { result -> openTimetable(result.key) },
    )

private val fileHomeParentNavKeyList =
    listOf(TopLevelNavigation.DEFAULT, TopLevelNavigation.More)
        .distinct()
        .map(TopLevelNavigation::key)
