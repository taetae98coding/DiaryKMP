package io.github.taetae98coding.diary.core.model.file

public sealed interface ContinuedFileUploadResult {
    public val name: String

    public data class Succeeded(
        override val name: String,
        val file: DiaryFile,
    ) : ContinuedFileUploadResult

    public data class TooLarge(
        override val name: String,
    ) : ContinuedFileUploadResult

    public data class Failed(
        override val name: String,
    ) : ContinuedFileUploadResult
}
