package io.github.taetae98coding.diary.core.network.api.file.exception

public class FileTooLargeRemoteException(
    override val message: String? = null,
    override val cause: Throwable? = null,
) : Exception(message, cause)
