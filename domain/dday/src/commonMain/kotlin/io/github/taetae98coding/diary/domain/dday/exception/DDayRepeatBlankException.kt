package io.github.taetae98coding.diary.domain.dday.exception

public class DDayRepeatBlankException(
    override val message: String? = null,
    override val cause: Throwable? = null,
) : Exception(message, cause)
