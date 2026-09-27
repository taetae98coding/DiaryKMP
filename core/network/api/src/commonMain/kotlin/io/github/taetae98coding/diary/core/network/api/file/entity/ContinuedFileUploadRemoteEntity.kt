package io.github.taetae98coding.diary.core.network.api.file.entity

public data class ContinuedFileUploadRemoteEntity(
    val name: String,
    val contentLength: Long,
    val sentBytes: Long,
)
