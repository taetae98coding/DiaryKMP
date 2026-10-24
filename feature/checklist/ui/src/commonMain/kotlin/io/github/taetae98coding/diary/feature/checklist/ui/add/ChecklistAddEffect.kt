package io.github.taetae98coding.diary.feature.checklist.ui.add

internal sealed interface ChecklistAddEffect {
    data object AddSucceeded : ChecklistAddEffect

    data object TitleBlank : ChecklistAddEffect
}
