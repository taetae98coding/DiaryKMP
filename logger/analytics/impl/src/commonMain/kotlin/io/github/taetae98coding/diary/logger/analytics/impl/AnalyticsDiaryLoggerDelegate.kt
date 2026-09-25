package io.github.taetae98coding.diary.logger.analytics.impl

import io.github.taetae98coding.diary.logger.analytics.api.AnalyticsEventLog
import io.github.taetae98coding.diary.logger.analytics.api.ScreenViewLog
import io.github.taetae98coding.diary.logger.core.DiaryLog
import io.github.taetae98coding.diary.logger.core.DiaryLoggerDelegate

public class AnalyticsDiaryLoggerDelegate : DiaryLoggerDelegate {
    override fun log(log: DiaryLog) {
        when (log) {
            is ScreenViewLog -> logScreenView(screenName = log.screenName)
            is AnalyticsEventLog -> logEvent(name = log.name, parameters = log.parameters.toAnalyticsParameterMap())
        }
    }
}

internal expect fun logScreenView(screenName: String)

internal expect fun logEvent(
    name: String,
    parameters: Map<String, Any>,
)
