package io.github.taetae98coding.diary.feature.more.ui.profile

internal sealed interface ProfileImageEditEffect {
    data object ChangeSucceeded : ProfileImageEditEffect

    data object ChangeFailed : ProfileImageEditEffect
}
