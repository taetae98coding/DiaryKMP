package io.github.taetae98coding.diary.work.mcp.tool.dday

import io.github.taetae98coding.diary.core.model.dday.DDay
import io.github.taetae98coding.diary.work.appaction.dday.toAppActionDDay
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonObject

internal fun DDay.toJson(today: LocalDate): JsonObject = toAppActionDDay(today = today).toJson()
