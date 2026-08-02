package io.github.taetae98coding.diary.domain.tag.exception

public class TagLinkSelfException(
    override val message: String? = null,
    override val cause: Throwable? = null,
) : Exception(message, cause)
