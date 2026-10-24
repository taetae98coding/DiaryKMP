package io.github.taetae98coding.diary.feature.checklist.ui

import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.navigation.navigateToDetail
import io.github.taetae98coding.diary.core.navigation.navigateUpTo
import io.github.taetae98coding.diary.feature.checklist.api.ChecklistAddNavKey
import io.github.taetae98coding.diary.feature.checklist.api.ChecklistDetailNavKey
import io.github.taetae98coding.diary.feature.checklist.api.ChecklistHomeNavKey
import kotlin.uuid.Uuid

internal fun NavBackStack<ScreenNavKey>.navigateToChecklistDetail(id: Uuid) {
    navigateToDetail(
        detailKey = ChecklistDetailNavKey(id = id),
        isDetailPaneKey = { key -> key is ChecklistDetailNavKey || key == ChecklistAddNavKey },
    )
}

internal fun NavBackStack<ScreenNavKey>.navigateUpFromChecklistHome() {
    navigateUpTo(homeKey = ChecklistHomeNavKey)
}
