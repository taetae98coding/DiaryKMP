package io.github.taetae98coding.diary.app.initializer

import io.github.taetae98coding.diary.logger.analytics.impl.AnalyticsDiaryLoggerDelegate
import io.github.taetae98coding.diary.logger.console.impl.ConsoleDiaryLoggerDelegate
import io.github.taetae98coding.diary.logger.core.DiaryLogger
import io.github.taetae98coding.diary.logger.crashlytics.impl.CrashlyticsDiaryLoggerDelegate

internal object LoggerInitializer {
    fun initialize(isDebug: Boolean): DiaryLogger {
        if (isDebug) {
            DiaryLogger.add(ConsoleDiaryLoggerDelegate())
        }

        DiaryLogger.add(CrashlyticsDiaryLoggerDelegate())
        DiaryLogger.add(AnalyticsDiaryLoggerDelegate())

        return DiaryLogger
    }
}
