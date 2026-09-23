package io.github.taetae98coding.diary.app.shared.navigation

import io.github.taetae98coding.diary.library.navigation3.ScreenNavKey
import io.github.taetae98coding.diary.feature.calendar.api.CalendarHomeNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoHomeNavKey
import io.github.taetae98coding.diary.feature.more.api.MoreHomeNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineHomeNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagHomeNavKey

internal enum class TopLevelNavigation(
    val key: ScreenNavKey,
) {
    Memo(key = MemoHomeNavKey),
    Tag(key = TagHomeNavKey),
    Calendar(key = CalendarHomeNavKey),
    Routine(key = RoutineHomeNavKey),
    More(key = MoreHomeNavKey),
    ;

    companion object {
        val DEFAULT: TopLevelNavigation = Calendar
    }
}
