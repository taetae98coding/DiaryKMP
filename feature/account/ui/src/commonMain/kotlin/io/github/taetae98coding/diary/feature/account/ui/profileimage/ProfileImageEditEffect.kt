package io.github.taetae98coding.diary.feature.account.ui.profileimage

internal sealed interface ProfileImageEditEffect {
    data object ChangeSucceeded : ProfileImageEditEffect

    data object ChangeFailed : ProfileImageEditEffect
}
