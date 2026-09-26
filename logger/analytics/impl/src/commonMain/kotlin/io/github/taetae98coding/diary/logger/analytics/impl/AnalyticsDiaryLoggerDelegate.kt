package io.github.taetae98coding.diary.logger.analytics.impl

import io.github.taetae98coding.diary.logger.analytics.api.AnalyticsEventLog
import io.github.taetae98coding.diary.logger.analytics.api.ScreenViewLog
import io.github.taetae98coding.diary.logger.core.DiaryLog
import io.github.taetae98coding.diary.logger.core.DiaryLoggerDelegate

// 플랫폼 기록은 데스크톱 앱에서 아무것도 남기지 않으므로, 담당 기준을 확인할 수 있게 기록하는 자리를 생성자로 받는다.
public class AnalyticsDiaryLoggerDelegate internal constructor(
    private val recordScreenView: (screenName: String) -> Unit,
    private val recordEvent: (name: String, parameters: Map<String, Any>) -> Unit,
) : DiaryLoggerDelegate {
    public constructor() : this(recordScreenView = ::logScreenView, recordEvent = ::logEvent)

    override fun log(log: DiaryLog) {
        when (log) {
            is ScreenViewLog -> recordScreenView(log.screenName)
            is AnalyticsEventLog -> recordEvent(log.name, log.parameters.toAnalyticsParameterMap())
        }
    }
}

internal expect fun logScreenView(screenName: String)

internal expect fun logEvent(
    name: String,
    parameters: Map<String, Any>,
)
