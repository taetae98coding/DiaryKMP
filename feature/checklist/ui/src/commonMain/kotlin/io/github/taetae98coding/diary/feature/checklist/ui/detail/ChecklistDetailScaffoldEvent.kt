package io.github.taetae98coding.diary.feature.checklist.ui.detail

import io.github.taetae98coding.diary.core.model.checklist.ChecklistItemSort
import kotlin.uuid.Uuid

internal sealed interface ChecklistDetailScaffoldEvent {
    data object ClickNavigateUp : ChecklistDetailScaffoldEvent

    data object ClickReset : ChecklistDetailScaffoldEvent

    data object ConfirmReset : ChecklistDetailScaffoldEvent

    data object ClickDelete : ChecklistDetailScaffoldEvent

    data object ClickUpdate : ChecklistDetailScaffoldEvent

    data object ClickSort : ChecklistDetailScaffoldEvent

    data class SelectSort(
        val sort: ChecklistItemSort,
    ) : ChecklistDetailScaffoldEvent

    data class ClickItem(
        val id: Uuid,
        val isChecked: Boolean,
    ) : ChecklistDetailScaffoldEvent

    data class ClickItemRemove(
        val id: Uuid,
    ) : ChecklistDetailScaffoldEvent

    data object ClickItemAdd : ChecklistDetailScaffoldEvent
}
