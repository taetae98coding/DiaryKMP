package io.github.taetae98coding.diary.logger.crashlytics.api

import io.github.taetae98coding.diary.logger.core.DiaryLogger

public fun DiaryLogger.logCrashlyticsFailure(
    source: Any,
    throwable: Throwable,
) {
    log(log = CrashlyticsLog(message = "${source::class.simpleName.orEmpty()} 실패", throwable = throwable))
}
