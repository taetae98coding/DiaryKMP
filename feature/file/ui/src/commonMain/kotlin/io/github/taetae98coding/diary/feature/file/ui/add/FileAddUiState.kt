package io.github.taetae98coding.diary.feature.file.ui.add

import io.github.taetae98coding.diary.core.model.file.FileUploadSource

internal data class FileAddUiState(
    val selectedFile: FileUploadSource? = null,
    val isUploading: Boolean = false,
)
