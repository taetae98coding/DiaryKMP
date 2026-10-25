package io.github.taetae98coding.diary.feature.file.ui.detail

import io.github.taetae98coding.diary.core.model.file.DiaryFile

internal sealed interface FileDetailUiState {
    data object Loading : FileDetailUiState

    data object Failed : FileDetailUiState

    data class Content(
        val file: DiaryFile,
        val isUpdateInProgress: Boolean = false,
        val isDownloadInProgress: Boolean = false,
    ) : FileDetailUiState
}
