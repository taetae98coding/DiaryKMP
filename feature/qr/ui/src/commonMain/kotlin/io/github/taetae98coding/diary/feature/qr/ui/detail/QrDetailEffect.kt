package io.github.taetae98coding.diary.feature.qr.ui.detail

internal sealed interface QrDetailEffect {
    data object UpdateSucceeded : QrDetailEffect

    data object ValueEmpty : QrDetailEffect

    data object DeleteSucceeded : QrDetailEffect
}
