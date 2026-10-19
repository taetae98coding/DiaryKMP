package io.github.taetae98coding.diary.feature.checklist.ui.detail

internal sealed interface ChecklistDetailItemEffect {
    data object AddSucceeded : ChecklistDetailItemEffect
}
