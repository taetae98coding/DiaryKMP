package io.github.taetae98coding.diary.domain.checklist.exception

public class ChecklistItemContentBlankException(
    override val message: String? = null,
    override val cause: Throwable? = null,
) : Exception(message, cause)
