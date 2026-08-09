package io.github.taetae98coding.diary.domain.place.exception

public class PlaceTitleBlankException(
    override val message: String? = null,
    override val cause: Throwable? = null,
) : Exception(message, cause)
