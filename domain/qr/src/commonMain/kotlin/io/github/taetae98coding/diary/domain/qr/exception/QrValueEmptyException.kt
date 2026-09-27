package io.github.taetae98coding.diary.domain.qr.exception

public class QrValueEmptyException(
    override val message: String? = null,
    override val cause: Throwable? = null,
) : Exception(message, cause)
