package io.github.taetae98coding.diary.feature.contact.ui

import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactAddNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactDetailNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactHomeNavKey
import kotlin.uuid.Uuid

internal fun NavBackStack<ScreenNavKey>.navigateToContactDetail(id: Uuid) {
    while (lastOrNull().let { key -> key is ContactDetailNavKey || key is ContactAddNavKey }) {
        removeLastOrNull()
    }

    add(ContactDetailNavKey(id = id))
}

internal fun NavBackStack<ScreenNavKey>.navigateUpFromContactHome() {
    val index = indexOfLast { key -> key == ContactHomeNavKey }
    if (index < 0) return

    while (size > index) {
        removeLastOrNull()
    }
}
