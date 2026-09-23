package io.github.taetae98coding.diary.feature.contact.ui

import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactHomeNavKey

internal fun NavBackStack<ScreenNavKey>.navigateUpFromContactHome() {
    val index = indexOfLast { key -> key == ContactHomeNavKey }
    if (index < 0) return

    while (size > index) {
        removeLastOrNull()
    }
}
