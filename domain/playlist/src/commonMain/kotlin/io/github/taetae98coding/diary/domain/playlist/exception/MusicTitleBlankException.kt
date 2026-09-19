package io.github.taetae98coding.diary.domain.playlist.exception

public class MusicTitleBlankException(
    override val message: String? = null,
    override val cause: Throwable? = null,
) : Exception(message, cause)
