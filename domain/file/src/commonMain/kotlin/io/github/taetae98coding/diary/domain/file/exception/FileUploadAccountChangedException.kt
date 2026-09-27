package io.github.taetae98coding.diary.domain.file.exception

public class FileUploadAccountChangedException(
    override val message: String? = null,
    override val cause: Throwable? = null,
) : Exception(message, cause)
