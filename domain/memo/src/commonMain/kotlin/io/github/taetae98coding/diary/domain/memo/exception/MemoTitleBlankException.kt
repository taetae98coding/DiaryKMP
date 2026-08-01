package io.github.taetae98coding.diary.domain.memo.exception

public class MemoTitleBlankException(
    override val message: String? = null,
    override val cause: Throwable? = null,
) : Exception(message, cause)
