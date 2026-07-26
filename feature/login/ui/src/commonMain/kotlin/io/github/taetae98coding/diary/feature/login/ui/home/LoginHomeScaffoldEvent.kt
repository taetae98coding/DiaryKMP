package io.github.taetae98coding.diary.feature.login.ui.home

internal sealed interface LoginHomeScaffoldEvent {
    data object ClickNavigateUp : LoginHomeScaffoldEvent

    data object ClickGoogleSignIn : LoginHomeScaffoldEvent

    data object ClickAppleSignIn : LoginHomeScaffoldEvent
}
