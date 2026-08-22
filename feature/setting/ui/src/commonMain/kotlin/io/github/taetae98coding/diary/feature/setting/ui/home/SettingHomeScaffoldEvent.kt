package io.github.taetae98coding.diary.feature.setting.ui.home

internal sealed interface SettingHomeScaffoldEvent {
    data object ClickNavigateUp : SettingHomeScaffoldEvent

    data class ClickItem(
        val item: SettingHomeItem,
    ) : SettingHomeScaffoldEvent
}
