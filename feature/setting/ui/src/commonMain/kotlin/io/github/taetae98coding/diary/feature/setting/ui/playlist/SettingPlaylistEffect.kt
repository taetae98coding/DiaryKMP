package io.github.taetae98coding.diary.feature.setting.ui.playlist

internal sealed interface SettingPlaylistEffect {
    data object SaveSucceeded : SettingPlaylistEffect

    data object SaveFailed : SettingPlaylistEffect
}
