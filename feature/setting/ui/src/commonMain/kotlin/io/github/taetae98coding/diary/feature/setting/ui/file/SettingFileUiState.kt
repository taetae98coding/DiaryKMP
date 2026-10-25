package io.github.taetae98coding.diary.feature.setting.ui.file

import io.github.taetae98coding.diary.core.model.file.FileDownloadDirectory

internal sealed interface SettingFileUiState {
    data object Loading : SettingFileUiState

    data class Content(
        val directory: FileDownloadDirectory?,
    ) : SettingFileUiState
}
