package io.github.taetae98coding.diary.work.mcp.tool.routine

import io.github.taetae98coding.diary.core.model.routine.RoutineCycle
import io.github.taetae98coding.diary.domain.routine.ROUTINE_UNIT_MAX_LENGTH
import io.github.taetae98coding.diary.work.appaction.routine.AppActionRoutine
import io.github.taetae98coding.diary.work.appaction.routine.AppActionRoutineDetail
import io.github.taetae98coding.diary.work.appaction.routine.AppActionRoutineProgress
import io.github.taetae98coding.diary.work.appaction.routine.AppActionRoutineRecord
import io.github.taetae98coding.diary.work.appaction.routine.appActionRoutineCycleList
import io.github.taetae98coding.diary.work.appaction.routine.toRoutineCycleOrNull
import io.github.taetae98coding.diary.work.appaction.routine.toRoutineCycleValue
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArgumentException
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArguments
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameter
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameterType
import io.github.taetae98coding.diary.work.mcp.tool.json.toJson
import io.github.taetae98coding.diary.work.mcp.tool.json.toJsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

internal const val CYCLE = "cycle"
internal const val UNIT = "unit"

internal fun cycleParameter(description: String): McpToolParameter =
    McpToolParameter(
        name = CYCLE,
        type = McpToolParameterType.STRING,
        description =
            "$description FREQ=DAILY is every day, FREQ=DAILY;BYDAY=MO,TU,WE,TH,FR is every day done on weekdays only, " +
                "FREQ=DAILY;BYDAY=SA,SU is every day done on weekends only, FREQ=WEEKLY;WKST=SU is every week from Sunday to Saturday, " +
                "FREQ=MONTHLY is every month from the 1st to the last day.",
        allowedValueList = appActionRoutineCycleList.map { cycle -> cycle.toRoutineCycleValue() },
    )

internal fun unitParameter(description: String): McpToolParameter =
    McpToolParameter(
        name = UNIT,
        type = McpToolParameterType.STRING,
        description = "$description Free text of at most $ROUTINE_UNIT_MAX_LENGTH characters without line breaks, such as times, ml or pages. Spaces around it are removed.",
    )

internal fun McpToolArguments.cycleOrNull(): RoutineCycle? {
    val value = stringOrNull(CYCLE) ?: return null

    return value.toRoutineCycleOrNull()
        ?: throw McpToolArgumentException(message = "'$CYCLE' must be one of ${appActionRoutineCycleList.joinToString { cycle -> cycle.toRoutineCycleValue() }}.")
}

internal fun AppActionRoutine.toJson(): JsonObject =
    buildJsonObject {
        put("id", id)
        put("title", title)
        put("description", description)
        put("color", color)
        put("startDate", startDate)
        put(CYCLE, cycle)
        put(UNIT, unit)
        put("goalAmount", goalAmount)
        put("amountPerRecord", amountPerRecord)
        put("isFinished", isFinished)
        put("createdAt", createdAt)
        put("updatedAt", updatedAt)
        put("progress", progress.toJson())
    }

internal fun AppActionRoutineDetail.toJson(): JsonObject =
    buildJsonObject {
        put("routine", routine.toJson())
        put("tags", tags.toJsonArray { tag -> tag.toJson() })
    }

private fun AppActionRoutineProgress.toJson(): JsonObject =
    buildJsonObject {
        put("status", status)
        cycleAmount?.let { amount -> put("cycleAmount", amount) }
        achievementRate?.let { rate -> put("achievementRate", rate) }
    }

internal fun AppActionRoutineRecord.toJson(): JsonObject =
    buildJsonObject {
        put("id", id)
        put("routineId", routineId)
        put("date", date)
        put("amount", amount)
        put("createdAt", createdAt)
        put("updatedAt", updatedAt)
    }
