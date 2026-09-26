package io.github.taetae98coding.diary.feature.qr.ui.add

internal sealed interface QrAddEffect {
    data object AddSucceeded : QrAddEffect

    data object TitleBlank : QrAddEffect

    data object ValueEmpty : QrAddEffect
}
