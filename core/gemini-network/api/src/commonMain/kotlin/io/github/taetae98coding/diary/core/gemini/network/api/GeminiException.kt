package io.github.taetae98coding.diary.core.gemini.network.api

public sealed class GeminiException(
    message: String,
    cause: Throwable?,
) : Exception(message, cause) {
    public class InvalidApiKey(
        cause: Throwable?,
    ) : GeminiException("Gemini API key is invalid", cause)

    public class QuotaExceeded(
        cause: Throwable?,
    ) : GeminiException("Gemini request quota is exceeded", cause)

    public class ModelNotFound(
        cause: Throwable?,
    ) : GeminiException("Gemini model is not found", cause)

    public class Blocked(
        detail: String,
    ) : GeminiException("Gemini blocked the request or the response: $detail", null)

    public class Truncated(
        detail: String,
    ) : GeminiException("Gemini stopped at the output length limit: $detail", null)

    public class Timeout(
        cause: Throwable?,
    ) : GeminiException("Gemini did not respond within the idle timeout", cause)

    public class Network(
        cause: Throwable?,
    ) : GeminiException("Could not connect to Gemini", cause)

    public class Server(
        cause: Throwable?,
    ) : GeminiException("Gemini could not process the request", cause)

    // Gemini가 돌려준 실패 설명을 사용자에게 그대로 보여 주므로 메시지와 분리해 들고 있다.
    public class Other(
        public val description: String,
        cause: Throwable?,
    ) : GeminiException("Gemini request failed: $description", cause)

    // 실패 원인은 오류 보고로 남으므로, 생성된 내용이 담길 수 있는 파싱 예외를 원인으로 잇지 않고 응답의 종료 사유만 메시지에 담는다.
    public class InvalidContent(
        detail: String,
    ) : GeminiException("Gemini did not return content in the requested structure: $detail", null)
}
