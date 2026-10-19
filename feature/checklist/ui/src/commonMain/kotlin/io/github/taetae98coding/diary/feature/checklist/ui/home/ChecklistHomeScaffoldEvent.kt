package io.github.taetae98coding.diary.feature.checklist.ui.home

import kotlin.uuid.Uuid

internal sealed interface ChecklistHomeScaffoldEvent {
    data object ClickNavigateUp : ChecklistHomeScaffoldEvent

    data object ClickAdd : ChecklistHomeScaffoldEvent

    data object Refresh : ChecklistHomeScaffoldEvent

    data class ClickChecklist(
        val id: Uuid,
    ) : ChecklistHomeScaffoldEvent

    data class DeleteChecklist(
        val id: Uuid,
    ) : ChecklistHomeScaffoldEvent
}
