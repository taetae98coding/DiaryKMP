package io.github.taetae98coding.diary.core.model.file

public sealed interface FileUploadStep {
    public val source: FileUploadSource

    public data class Started(
        override val source: FileUploadSource,
    ) : FileUploadStep

    public data class Sent(
        override val source: FileUploadSource,
        val sentBytes: Long,
    ) : FileUploadStep

    public data class Completed(
        override val source: FileUploadSource,
        val file: DiaryFile,
    ) : FileUploadStep
}
