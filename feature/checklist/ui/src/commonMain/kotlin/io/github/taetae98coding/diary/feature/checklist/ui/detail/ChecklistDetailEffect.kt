package io.github.taetae98coding.diary.feature.checklist.ui.detail

internal sealed interface ChecklistDetailEffect {
    data object UpdateSucceeded : ChecklistDetailEffect

    data object DeleteSucceeded : ChecklistDetailEffect
}
