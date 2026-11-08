package io.github.taetae98coding.diary.feature.dday.ui.add

internal sealed interface DDayAddEffect {
    data object AddSucceeded : DDayAddEffect

    data object TitleBlank : DDayAddEffect

    data object RepeatBlank : DDayAddEffect
}
