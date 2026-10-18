package io.github.taetae98coding.diary.logger.console.impl

import io.github.taetae98coding.diary.logger.console.api.ConsoleLog
import io.github.taetae98coding.diary.logger.console.api.DEFAULT_CONSOLE_TAG
import io.github.taetae98coding.diary.logger.core.DiaryLog
import io.github.taetae98coding.diary.logger.core.DiaryLoggerDelegate

public class ConsoleDiaryLoggerDelegate internal constructor(
    private val print: (tag: String, message: String, throwable: Throwable?) -> Unit,
) : DiaryLoggerDelegate {
    public constructor() : this(print = ::printLog)

    override fun log(log: DiaryLog) {
        if (log is ConsoleLog) {
            print(log.tag, log.message, log.throwable)
        } else {
            print(DEFAULT_CONSOLE_TAG, log.toString(), null)
        }
    }
}

internal expect fun printLog(
    tag: String,
    message: String,
    throwable: Throwable?,
)

internal fun consoleMessage(
    tag: String,
    message: String,
    throwable: Throwable?,
): String =
    buildString {
        append('[')
        append(tag)
        append("] ")
        append(message)

        if (throwable != null) {
            appendLine()
            append(throwable.stackTraceToString())
        }
    }
