package io.github.taetae98coding.diary.logger.console.impl

internal actual fun printLog(
    tag: String,
    message: String,
    throwable: Throwable?,
) {
    println(consoleMessage(tag = tag, message = message, throwable = throwable))
}
