package io.github.taetae98coding.diary.logger.analytics.impl

import io.github.taetae98coding.diary.logger.analytics.api.ScreenViewLog
import io.github.taetae98coding.diary.logger.core.DiaryLog
import io.github.taetae98coding.diary.logger.core.DiaryLoggerDelegate

public class AnalyticsDiaryLoggerDelegate : DiaryLoggerDelegate {
    override fun log(log: DiaryLog) {
        if (log is ScreenViewLog) {
            logScreenView(screenName = log.screenName)
        }
    }
}

internal expect fun logScreenView(screenName: String)
