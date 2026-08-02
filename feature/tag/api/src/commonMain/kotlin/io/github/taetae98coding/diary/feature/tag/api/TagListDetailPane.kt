package io.github.taetae98coding.diary.feature.tag.api

import androidx.navigation3.runtime.NavKey

public fun List<NavKey>.isTagListDetailPane(key: NavKey): Boolean {
    val index = lastIndexOf(key)
    if (index < 0 || !key.isTagDetailPaneKey()) return false

    return subList(0, index).lastOrNull { belowKey -> !belowKey.isTagDetailPaneKey() } == TagHomeNavKey
}

private fun NavKey.isTagDetailPaneKey(): Boolean = this is TagAddNavKey || this is TagDetailNavKey
