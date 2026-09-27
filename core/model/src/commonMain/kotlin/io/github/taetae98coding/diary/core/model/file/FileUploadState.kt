package io.github.taetae98coding.diary.core.model.file

public sealed interface FileUploadState {
    public data object Idle : FileUploadState

    public data class Uploading(
        val percent: Int?,
    ) : FileUploadState
}
