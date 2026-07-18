package io.github.taetae98coding.diary.logger.crashlytics.impl

import io.github.taetae98coding.diary.logger.core.DiaryLog
import io.github.taetae98coding.diary.logger.core.DiaryLoggerDelegate
import io.github.taetae98coding.diary.logger.crashlytics.api.CrashlyticsLog

public class CrashlyticsDiaryLoggerDelegate : DiaryLoggerDelegate {
    override fun log(log: DiaryLog) {
        if (log is CrashlyticsLog) {
            recordException(message = log.message, throwable = log.throwable)
        }
    }
}

internal expect fun recordException(
    message: String,
    throwable: Throwable,
)
