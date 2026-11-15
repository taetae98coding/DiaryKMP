package io.github.taetae98coding.diary.domain.routine.exception

public class RoutineGoalAmountInvalidException(
    override val message: String? = null,
    override val cause: Throwable? = null,
) : Exception(message, cause)
