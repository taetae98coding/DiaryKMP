package io.github.taetae98coding.diary.feature.checklist.ui.home

internal sealed interface ChecklistHomeScaffoldEvent {
    data object ClickNavigateUp : ChecklistHomeScaffoldEvent
}
