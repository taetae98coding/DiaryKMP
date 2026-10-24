package io.github.taetae98coding.diary.feature.checklist.ui.detail

import io.github.taetae98coding.diary.core.model.checklist.ChecklistItem
import io.github.taetae98coding.diary.core.model.checklist.ChecklistItemSort

internal data class ChecklistDetailItemUiState(
    val itemList: List<ChecklistItem> = emptyList(),
    val isLoaded: Boolean = false,
    val sort: ChecklistItemSort = ChecklistItemSort.ADDED,
    val isAddInProgress: Boolean = false,
    val isResetInProgress: Boolean = false,
) {
    val isEmpty: Boolean
        get() = isLoaded && itemList.isEmpty()

    val isResetEnabled: Boolean
        get() = itemList.any(ChecklistItem::isChecked)
}
