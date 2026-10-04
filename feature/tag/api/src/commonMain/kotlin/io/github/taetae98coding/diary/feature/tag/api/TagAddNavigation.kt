package io.github.taetae98coding.diary.feature.tag.api

import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey

public fun NavBackStack<ScreenNavKey>.navigateToTagAddFromFilter() {
    removeLastOrNull()
    add(TagAddNavKey())
}
