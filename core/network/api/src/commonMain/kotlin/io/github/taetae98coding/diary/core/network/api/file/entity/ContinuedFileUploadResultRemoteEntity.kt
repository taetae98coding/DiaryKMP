package io.github.taetae98coding.diary.core.network.api.file.entity

public sealed interface ContinuedFileUploadResultRemoteEntity {
    public val name: String

    public data class Succeeded(
        override val name: String,
        val file: FileRemoteEntity,
    ) : ContinuedFileUploadResultRemoteEntity

    public data class TooLarge(
        override val name: String,
    ) : ContinuedFileUploadResultRemoteEntity

    public data class Failed(
        override val name: String,
    ) : ContinuedFileUploadResultRemoteEntity
}
