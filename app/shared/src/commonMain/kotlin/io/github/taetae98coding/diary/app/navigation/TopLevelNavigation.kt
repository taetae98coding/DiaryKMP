package io.github.taetae98coding.diary.app.navigation

import androidx.navigation3.runtime.NavKey
import io.github.taetae98coding.diary.feature.calendar.api.CalendarHomeNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoHomeNavKey
import io.github.taetae98coding.diary.feature.more.api.MoreHomeNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineHomeNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagHomeNavKey

internal enum class TopLevelNavigation(
    val key: NavKey,
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
