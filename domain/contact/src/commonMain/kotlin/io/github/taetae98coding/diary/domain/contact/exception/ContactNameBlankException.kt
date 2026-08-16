package io.github.taetae98coding.diary.domain.contact.exception

public class ContactNameBlankException(
    override val message: String? = null,
    override val cause: Throwable? = null,
) : Exception(message, cause)
