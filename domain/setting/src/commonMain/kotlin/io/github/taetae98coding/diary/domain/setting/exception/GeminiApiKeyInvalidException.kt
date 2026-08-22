package io.github.taetae98coding.diary.domain.setting.exception

public class GeminiApiKeyInvalidException(
    override val message: String? = null,
    override val cause: Throwable? = null,
) : Exception(message, cause)
