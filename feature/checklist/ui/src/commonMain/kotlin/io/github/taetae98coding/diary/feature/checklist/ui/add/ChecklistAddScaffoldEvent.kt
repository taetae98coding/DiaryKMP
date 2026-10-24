package io.github.taetae98coding.diary.feature.checklist.ui.add

internal sealed interface ChecklistAddScaffoldEvent {
    data object ClickNavigateUp : ChecklistAddScaffoldEvent

    data object ClickAdd : ChecklistAddScaffoldEvent

    data object ClickItemAdd : ChecklistAddScaffoldEvent

    data class ClickItemRemove(
        val key: String,
    ) : ChecklistAddScaffoldEvent
}
