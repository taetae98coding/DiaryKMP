package io.github.taetae98coding.diary.feature.account.ui.profile

internal sealed interface ProfileDeleteEffect {
    data object RequestAppleAuthorization : ProfileDeleteEffect

    data object DeleteFailed : ProfileDeleteEffect
}
