package io.github.taetae98coding.diary.core.model.file

public sealed interface FileDownloadResult {
    public data class Saved(
        val name: String,
    ) : FileDownloadResult

    public data object HandedToBrowser : FileDownloadResult
}
