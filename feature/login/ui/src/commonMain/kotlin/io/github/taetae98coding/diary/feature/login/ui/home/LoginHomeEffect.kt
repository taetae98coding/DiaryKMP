package io.github.taetae98coding.diary.feature.login.ui.home

internal sealed interface LoginHomeEffect {
    data object SignInSucceeded : LoginHomeEffect

    data object SignInFailed : LoginHomeEffect
}
