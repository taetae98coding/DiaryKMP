package io.github.taetae98coding.diary.core.file.api.exception

public class FileDownloadDirectoryUnavailableException(
    override val message: String? = null,
    override val cause: Throwable? = null,
) : Exception(message, cause)
