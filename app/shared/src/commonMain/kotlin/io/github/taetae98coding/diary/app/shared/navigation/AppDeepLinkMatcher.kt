package io.github.taetae98coding.diary.app.shared.navigation

import androidx.navigation3.runtime.deeplink.BackStackMatcher
import androidx.navigation3.runtime.deeplink.DeepLinkMatcher
import androidx.navigation3.runtime.deeplink.DeepLinkRequest
import androidx.navigation3.runtime.deeplink.DeepLinkUri
import androidx.navigation3.runtime.deeplink.UriDeepLinkMatcher
import androidx.navigation3.runtime.deeplink.withBackStack
import io.github.taetae98coding.diary.core.navigation.ScreenDeepLink
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.file.api.FileHomeNavKey
import io.github.taetae98coding.diary.feature.timetable.api.CalendarTimetableNavKey

internal object AppDeepLinkMatcher {
    val fileHome: BackStackMatcher<FileHomeNavKey, ScreenNavKey> =
        UriDeepLinkMatcher(DeepLinkUri(ScreenDeepLink.FILE_HOME), FileHomeNavKey.serializer())
            .withBackStack { result ->
                listOf(TopLevelNavigation.DEFAULT, TopLevelNavigation.More)
                    .distinct()
                    .map(TopLevelNavigation::key) + result.key
            }

    val timetable: UriDeepLinkMatcher<CalendarTimetableNavKey> =
        UriDeepLinkMatcher(DeepLinkUri(ScreenDeepLink.TIMETABLE), CalendarTimetableNavKey.serializer())
}

// 주소의 값을 화면 키의 값으로 읽지 못하면(날짜가 아닌 date 등) 매처가 예외를 던지므로, 그런 주소는 맞지 않는 주소로 다룬다.
internal fun <T : Any, R : DeepLinkMatcher.MatchResult<T>> DeepLinkMatcher<T, R>.matchOrNull(request: DeepLinkRequest): R? =
    try {
        match(request)
    } catch (_: IllegalArgumentException) {
        null
    }
