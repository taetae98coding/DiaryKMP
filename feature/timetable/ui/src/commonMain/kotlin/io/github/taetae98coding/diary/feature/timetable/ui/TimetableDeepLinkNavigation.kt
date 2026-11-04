package io.github.taetae98coding.diary.feature.timetable.ui

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.deeplink.DeepLinkUri
import androidx.navigation3.runtime.deeplink.UriDeepLinkMatcher
import io.github.taetae98coding.diary.core.navigation.DeepLinkNavigation
import io.github.taetae98coding.diary.core.navigation.ScreenDeepLink
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.navigation.toDeepLinkNavigation
import io.github.taetae98coding.diary.feature.timetable.api.CalendarTimetableNavKey
import kotlin.uuid.Uuid

public fun timetableDeepLinkNavigation(): DeepLinkNavigation =
    UriDeepLinkMatcher(DeepLinkUri(ScreenDeepLink.TIMETABLE), CalendarTimetableNavKey.serializer())
        .toDeepLinkNavigation { result -> openTimetable(result.key) }

private fun NavBackStack<ScreenNavKey>.openTimetable(navKey: CalendarTimetableNavKey) {
    val openedKey = navKey.copy(id = Uuid.random())

    if (lastOrNull() is CalendarTimetableNavKey) {
        this[lastIndex] = openedKey
    } else {
        add(openedKey)
    }
}
