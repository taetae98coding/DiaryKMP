package io.github.taetae98coding.diary.logger.crashlytics.api

import io.github.taetae98coding.diary.logger.core.DiaryLog

public data class CrashlyticsLog(
    val message: String,
    val throwable: Throwable,
) : DiaryLog
