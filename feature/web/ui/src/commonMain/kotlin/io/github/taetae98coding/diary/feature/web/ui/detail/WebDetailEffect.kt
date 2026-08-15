package io.github.taetae98coding.diary.feature.web.ui.detail

internal sealed interface WebDetailEffect {
    data object UpdateSucceeded : WebDetailEffect

    data object HeaderNameBlank : WebDetailEffect

    data object DeleteSucceeded : WebDetailEffect
}
