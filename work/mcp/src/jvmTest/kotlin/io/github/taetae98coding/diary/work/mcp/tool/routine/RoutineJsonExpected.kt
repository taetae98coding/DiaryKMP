package io.github.taetae98coding.diary.work.mcp.tool.routine

import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.work.appaction.routine.toAppActionRoutine
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonObject

internal fun RoutineSummary.toJson(today: LocalDate): JsonObject = toAppActionRoutine(today = today).toJson()
