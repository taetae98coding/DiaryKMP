package io.github.taetae98coding.diary.domain.setting.exception

public sealed class GeminiRequestException(
    cause: Throwable?,
) : Exception(cause?.message, cause) {
    public class QuotaExceeded(
        cause: Throwable?,
    ) : GeminiRequestException(cause)

    public class ModelNotFound(
        cause: Throwable?,
    ) : GeminiRequestException(cause)

    public class Blocked(
        cause: Throwable?,
    ) : GeminiRequestException(cause)

    public class Truncated(
        cause: Throwable?,
    ) : GeminiRequestException(cause)

    public class Timeout(
        cause: Throwable?,
    ) : GeminiRequestException(cause)

    public class Network(
        cause: Throwable?,
    ) : GeminiRequestException(cause)

    public class Server(
        cause: Throwable?,
    ) : GeminiRequestException(cause)

    public class Other(
        public val description: String,
        cause: Throwable?,
    ) : GeminiRequestException(cause)
}
