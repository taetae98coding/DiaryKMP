package io.github.taetae98coding.diary.app.initializer

import io.github.taetae98coding.diary.logger.console.impl.ConsoleDiaryLoggerDelegate
import io.github.taetae98coding.diary.logger.core.DiaryLogger
import io.github.taetae98coding.diary.logger.crashlytics.impl.CrashlyticsDiaryLoggerDelegate

public object LoggerInitializer {
    public fun initialize(isDebug: Boolean) {
        if (isDebug) {
            DiaryLogger.add(ConsoleDiaryLoggerDelegate())
        }

        DiaryLogger.add(CrashlyticsDiaryLoggerDelegate())
    }
}
