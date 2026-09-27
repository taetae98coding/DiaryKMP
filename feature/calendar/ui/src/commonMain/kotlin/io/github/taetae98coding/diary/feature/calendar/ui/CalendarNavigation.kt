package io.github.taetae98coding.diary.feature.calendar.ui

import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagAddNavKey

internal fun NavBackStack<ScreenNavKey>.navigateToTagAddFromCalendarHomeFilter() {
    removeLastOrNull()
    add(TagAddNavKey())
}
