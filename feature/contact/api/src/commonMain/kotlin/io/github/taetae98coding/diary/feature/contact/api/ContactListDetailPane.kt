package io.github.taetae98coding.diary.feature.contact.api

import androidx.navigation3.runtime.NavKey

public fun List<NavKey>.isContactListDetailPane(key: NavKey): Boolean {
    val index = lastIndexOf(key)
    if (index < 0 || !key.isContactDetailPaneKey()) return false

    return subList(0, index).lastOrNull { belowKey -> !belowKey.isContactDetailPaneKey() } == ContactHomeNavKey
}

private fun NavKey.isContactDetailPaneKey(): Boolean = this is ContactAddNavKey || this is ContactDetailNavKey
