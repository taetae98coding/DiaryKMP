package io.github.taetae98coding.diary.feature.more.ui.home

import io.github.taetae98coding.diary.feature.more.ui.home.menu.MoreHomeMenu

internal sealed interface MoreHomeScaffoldEvent {
    data object ClickAccountCard : MoreHomeScaffoldEvent

    data class ClickProfileImage(
        val imageUrl: String,
    ) : MoreHomeScaffoldEvent

    data object ClickSetting : MoreHomeScaffoldEvent

    data object ClickSignIn : MoreHomeScaffoldEvent

    data object ClickSignOut : MoreHomeScaffoldEvent

    data object ConfirmSignOut : MoreHomeScaffoldEvent

    data object CancelSignOut : MoreHomeScaffoldEvent

    data class ClickMenu(
        val menu: MoreHomeMenu,
    ) : MoreHomeScaffoldEvent
}
