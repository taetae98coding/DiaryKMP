package io.github.taetae98coding.diary.core.model.file

public data class FileUploadSource(
    val uri: FileUri,
    val name: String,
    val mimeType: String,
    val size: Long,
)
