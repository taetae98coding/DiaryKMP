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

    return key == ChecklistHomeNavKey || (key is ChecklistAddNavKey && isChecklistListDetailPane(key))
}

// 초기 태그가 있는 ChecklistAdd는 TagDetail 체크리스트 탭에서 진입한 것이다.
private fun ScreenNavKey.isChecklistDetailPaneKey(): Boolean = (this is ChecklistAddNavKey && initialTagId == null) || (this is ChecklistDetailNavKey && !isStandalone)
