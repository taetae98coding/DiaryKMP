package io.github.taetae98coding.diary.domain.qr.exception

public class QrTitleBlankException(
    override val message: String? = null,
    override val cause: Throwable? = null,
) : Exception(message, cause)
