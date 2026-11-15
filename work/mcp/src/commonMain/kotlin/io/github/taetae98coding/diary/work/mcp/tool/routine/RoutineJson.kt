package io.github.taetae98coding.diary.work.mcp.tool.routine

import io.github.taetae98coding.diary.core.model.routine.RoutineCycle
import io.github.taetae98coding.diary.core.model.routine.RoutineRecord
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.domain.routine.ROUTINE_UNIT_MAX_LENGTH
import io.github.taetae98coding.diary.work.appaction.routine.AppActionRoutineProgress
import io.github.taetae98coding.diary.work.appaction.routine.appActionRoutineCycleList
import io.github.taetae98coding.diary.work.appaction.routine.toAppActionRoutine
import io.github.taetae98coding.diary.work.appaction.routine.toAppActionRoutineRecord
import io.github.taetae98coding.diary.work.appaction.routine.toRoutineCycleOrNull
import io.github.taetae98coding.diary.work.appaction.routine.toRoutineCycleValue
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArgumentException
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArguments
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameter
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameterType
import kotlinx.datetime.LocalDate
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

internal fun RoutineSummary.toJson(today: LocalDate): JsonObject {
    val routine = toAppActionRoutine(today = today)

    return buildJsonObject {
        put("id", routine.id)
        put("title", routine.title)
        put("description", routine.description)
        put("color", routine.color)
        put("startDate", routine.startDate)
        put(CYCLE, routine.cycle)
        put(UNIT, routine.unit)
        put("goalAmount", routine.goalAmount)
        put("amountPerRecord", routine.amountPerRecord)
        put("isFinished", routine.isFinished)
        put("createdAt", routine.createdAt)
        put("updatedAt", routine.updatedAt)
        put("progress", routine.progress.toJson())
    }
}

private fun AppActionRoutineProgress.toJson(): JsonObject =
    buildJsonObject {
        put("status", status)
        cycleAmount?.let { amount -> put("cycleAmount", amount) }
        achievementRate?.let { rate -> put("achievementRate", rate) }
    }

internal fun RoutineRecord.toJson(): JsonObject {
    val record = toAppActionRoutineRecord()

    return buildJsonObject {
        put("id", record.id)
        put("routineId", record.routineId)
        put("date", record.date)
        put("amount", record.amount)
        put("createdAt", record.createdAt)
        put("updatedAt", record.updatedAt)
    }
}
