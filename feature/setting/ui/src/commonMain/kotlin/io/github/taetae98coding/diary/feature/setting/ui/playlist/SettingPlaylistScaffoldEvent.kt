package io.github.taetae98coding.diary.feature.setting.ui.playlist

internal sealed interface SettingPlaylistScaffoldEvent {
    data object ClickNavigateUp : SettingPlaylistScaffoldEvent

    data object ClickSave : SettingPlaylistScaffoldEvent
}
