package io.github.taetae98coding.diary.domain.web.exception

public class WebHeaderNameBlankException(
    override val message: String? = null,
    override val cause: Throwable? = null,
) : Exception(message, cause)
