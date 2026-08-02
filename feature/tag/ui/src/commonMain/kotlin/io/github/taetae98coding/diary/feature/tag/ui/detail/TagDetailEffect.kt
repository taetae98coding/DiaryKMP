package io.github.taetae98coding.diary.feature.tag.ui.detail

internal sealed interface TagDetailEffect {
    data object UpdateSucceeded : TagDetailEffect

    data object DeleteSucceeded : TagDetailEffect
}
