package io.github.taetae98coding.diary.feature.contact.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey

public fun List<ScreenNavKey>.isContactListDetailPane(key: ScreenNavKey): Boolean {
    val index = lastIndexOf(key)
    if (index < 0 || !key.isContactDetailPaneKey()) return false

    return subList(0, index).lastOrNull { belowKey -> !belowKey.isContactDetailPaneKey() } == ContactHomeNavKey
}

private fun ScreenNavKey.isContactDetailPaneKey(): Boolean = this is ContactAddNavKey || this is ContactDetailNavKey
