package io.github.taetae98coding.diary.core.gemini.network.api

public sealed class GeminiException(
    message: String,
    cause: Throwable?,
) : Exception(message, cause) {
    public class InvalidApiKey(
        cause: Throwable?,
    ) : GeminiException("Gemini API key is invalid", cause)

    public class InvalidContent(
        cause: Throwable?,
    ) : GeminiException("Gemini did not return content in the requested structure", cause)
}
