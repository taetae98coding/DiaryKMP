package io.github.taetae98coding.diary.feature.tag.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey

public fun List<ScreenNavKey>.isTagListDetailPane(key: ScreenNavKey): Boolean {
    val index = lastIndexOf(key)
    if (index < 0 || !key.isTagDetailPaneKey()) return false

    return subList(0, index).lastOrNull { belowKey -> !belowKey.isTagDetailPaneKey() } == TagHomeNavKey
}

private fun ScreenNavKey.isTagDetailPaneKey(): Boolean = this is TagAddNavKey || this is TagDetailNavKey
