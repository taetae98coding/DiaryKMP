package io.github.taetae98coding.diary.logger.console.api

@Suppress("ThrowingExceptionsWithoutMessageOrCause")
internal actual fun defaultConsoleTag(): String {
    val consoleLogClassName = requireNotNull(ConsoleLog::class.qualifiedName)
    val element =
        Throwable().stackTrace.firstOrNull { element ->
            !element.className.startsWith(consoleLogClassName)
        }

    return element
        ?.className
        ?.substringAfterLast('.')
        ?.substringBefore('$')
        ?: DEFAULT_CONSOLE_TAG
}
