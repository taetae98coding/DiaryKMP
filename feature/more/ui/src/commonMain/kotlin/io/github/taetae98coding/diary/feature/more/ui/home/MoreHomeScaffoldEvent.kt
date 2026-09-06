package io.github.taetae98coding.diary.feature.more.ui.home

internal sealed interface MoreHomeScaffoldEvent {
    data object ClickProfile : MoreHomeScaffoldEvent

    data object ClickSetting : MoreHomeScaffoldEvent

    data object ClickSignIn : MoreHomeScaffoldEvent

    data object ClickSignOut : MoreHomeScaffoldEvent

    data class ClickMenu(
        val menu: MoreHomeMenu,
    ) : MoreHomeScaffoldEvent
}
