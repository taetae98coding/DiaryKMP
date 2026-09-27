package io.github.taetae98coding.diary.feature.web.ui

import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.web.api.WebAddNavKey

internal fun NavBackStack<ScreenNavKey>.navigateToWebAddFromHome() {
    add(WebAddNavKey())
}
