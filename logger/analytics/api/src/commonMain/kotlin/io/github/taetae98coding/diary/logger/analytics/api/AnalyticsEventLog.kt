package io.github.taetae98coding.diary.logger.analytics.api

import io.github.taetae98coding.diary.logger.core.DiaryLog
import kotlinx.serialization.json.JsonObject

public data class AnalyticsEventLog(
    val name: String,
    val parameters: JsonObject,
) : DiaryLog
