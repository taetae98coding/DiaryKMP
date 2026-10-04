package io.github.taetae98coding.diary.feature.contact.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.navigation.isListDetailPane

public fun List<ScreenNavKey>.isContactListDetailPane(key: ScreenNavKey): Boolean =
    isListDetailPane(
        key = key,
        homeKey = ContactHomeNavKey,
        isDetailPaneKey = ScreenNavKey::isContactDetailPaneKey,
    )

public fun List<ScreenNavKey>.isContactAddOnDetailPane(): Boolean {
    val key = lastOrNull() ?: return false

    return key == ContactHomeNavKey || (key is ContactAddNavKey && isContactListDetailPane(key))
}

private fun ScreenNavKey.isContactDetailPaneKey(): Boolean = this is ContactAddNavKey || this is ContactDetailNavKey
