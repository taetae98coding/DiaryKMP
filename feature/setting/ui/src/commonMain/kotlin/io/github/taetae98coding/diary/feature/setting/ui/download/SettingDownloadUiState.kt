package io.github.taetae98coding.diary.feature.setting.ui.download

import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadProxySetting

internal sealed interface SettingDownloadUiState {
    data object Loading : SettingDownloadUiState

    data class Serving(
        val addressList: List<String>,
    ) : SettingDownloadUiState

    data object Unavailable : SettingDownloadUiState

    data class Consumer(
        val setting: MusicDownloadProxySetting,
        val isInProgress: Boolean = false,
    ) : SettingDownloadUiState
}
