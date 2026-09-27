package io.github.taetae98coding.diary.logger.crashlytics.impl

import io.github.taetae98coding.diary.logger.core.DiaryLog
import io.github.taetae98coding.diary.logger.core.DiaryLoggerDelegate
import io.github.taetae98coding.diary.logger.crashlytics.api.CrashlyticsLog

// 플랫폼 기록은 데스크톱 앱에서 아무것도 남기지 않으므로, 담당 기준을 확인할 수 있게 기록하는 자리를 생성자로 받는다.
public class CrashlyticsDiaryLoggerDelegate internal constructor(
    private val record: (message: String, throwable: Throwable) -> Unit,
) : DiaryLoggerDelegate {
    public constructor() : this(record = ::recordException)

    override fun log(log: DiaryLog) {
        if (log is CrashlyticsLog) {
            record(log.message, log.throwable)
        }
    }
}

internal expect fun recordException(
    message: String,
    throwable: Throwable,
)
