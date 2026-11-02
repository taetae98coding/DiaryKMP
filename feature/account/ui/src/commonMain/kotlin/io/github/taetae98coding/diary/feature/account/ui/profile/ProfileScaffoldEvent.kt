package io.github.taetae98coding.diary.feature.account.ui.profile

internal sealed interface ProfileScaffoldEvent {
    data object ClickNavigateUp : ProfileScaffoldEvent

    data object ClickChangeProfileImage : ProfileScaffoldEvent

    data object ClickDeleteAccount : ProfileScaffoldEvent

    data object ConfirmDeleteAccount : ProfileScaffoldEvent

    data object CancelDeleteAccount : ProfileScaffoldEvent
}
