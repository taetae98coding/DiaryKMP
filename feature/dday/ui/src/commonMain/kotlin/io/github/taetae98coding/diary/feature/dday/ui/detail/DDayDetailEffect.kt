package io.github.taetae98coding.diary.feature.dday.ui.detail

internal sealed interface DDayDetailEffect {
    data object UpdateSucceeded : DDayDetailEffect

    data object RepeatBlank : DDayDetailEffect

    data object DeleteSucceeded : DDayDetailEffect
}
