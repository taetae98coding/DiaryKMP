package io.github.taetae98coding.diary.feature.account.ui.profileimage

internal sealed interface ProfileImageEditScaffoldEvent {
    data object ClickNavigateUp : ProfileImageEditScaffoldEvent

    data object ClickChoosePhoto : ProfileImageEditScaffoldEvent

    data object ClickApply : ProfileImageEditScaffoldEvent
}
