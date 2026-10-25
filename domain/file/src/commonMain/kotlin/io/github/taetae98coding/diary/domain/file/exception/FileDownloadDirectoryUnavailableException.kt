package io.github.taetae98coding.diary.domain.file.exception

public class FileDownloadDirectoryUnavailableException(
    override val message: String? = null,
    override val cause: Throwable? = null,
) : Exception(message, cause)
