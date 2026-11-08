package io.github.taetae98coding.diary.work.appaction.core

public sealed class AppActionException(
    override val message: String,
    cause: Throwable?,
) : Exception(message, cause) {
    public class InvalidArgument(
        message: String,
    ) : AppActionException(message = message, cause = null)

    public class Failure(
        message: String,
        cause: Throwable? = null,
    ) : AppActionException(message = message, cause = cause)
}
