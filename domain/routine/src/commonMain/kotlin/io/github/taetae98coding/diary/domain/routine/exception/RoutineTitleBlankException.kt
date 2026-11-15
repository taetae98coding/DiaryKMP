package io.github.taetae98coding.diary.domain.routine.exception

public class RoutineTitleBlankException(
    override val message: String? = null,
    override val cause: Throwable? = null,
) : Exception(message, cause)
