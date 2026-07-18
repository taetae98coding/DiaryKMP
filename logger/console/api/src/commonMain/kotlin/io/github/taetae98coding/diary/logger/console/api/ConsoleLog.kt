package io.github.taetae98coding.diary.logger.console.api

import io.github.taetae98coding.diary.logger.core.DiaryLog

public data class ConsoleLog(
    val tag: String = defaultConsoleTag(),
    val message: String,
    val throwable: Throwable? = null,
) : DiaryLog

public const val DEFAULT_CONSOLE_TAG: String = "Diary"

internal expect fun defaultConsoleTag(): String
