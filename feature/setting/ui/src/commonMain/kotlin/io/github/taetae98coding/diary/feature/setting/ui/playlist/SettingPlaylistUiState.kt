package io.github.taetae98coding.diary.feature.setting.ui.playlist

import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadProxySetting

internal sealed interface SettingPlaylistUiState {
    data object Loading : SettingPlaylistUiState

    data class Serving(
        val addressList: List<String>,
    ) : SettingPlaylistUiState

    data object Unavailable : SettingPlaylistUiState

    data class Consumer(
        val setting: MusicDownloadProxySetting,
        val isInProgress: Boolean = false,
    ) : SettingPlaylistUiState
}
