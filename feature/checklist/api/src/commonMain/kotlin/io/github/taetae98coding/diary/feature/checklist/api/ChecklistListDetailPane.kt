package io.github.taetae98coding.diary.feature.checklist.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.navigation.isListDetailPane

public fun List<ScreenNavKey>.isChecklistListDetailPane(key: ScreenNavKey): Boolean =
    isListDetailPane(
        key = key,
        homeKey = ChecklistHomeNavKey,
        isDetailPaneKey = ScreenNavKey::isChecklistDetailPaneKey,
    )

public fun List<ScreenNavKey>.isChecklistAddOnDetailPane(): Boolean {
    val key = lastOrNull() ?: return false

    return key == ChecklistHomeNavKey || (key == ChecklistAddNavKey && isChecklistListDetailPane(key))
}

private fun ScreenNavKey.isChecklistDetailPaneKey(): Boolean = this is ChecklistAddNavKey || this is ChecklistDetailNavKey
