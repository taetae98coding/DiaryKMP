package io.github.taetae98coding.diary.work.mcp.tool.dday

import io.github.taetae98coding.diary.core.model.dday.CalendarDDay
import io.github.taetae98coding.diary.core.model.dday.DDay
import io.github.taetae98coding.diary.core.model.dday.DDayOverflowPolicy
import io.github.taetae98coding.diary.domain.dday.repeat.DDayRepeatInput
import io.github.taetae98coding.diary.work.appaction.calendar.toAppActionCalendarDDay
import io.github.taetae98coding.diary.work.appaction.dday.AppActionDDayCount
import io.github.taetae98coding.diary.work.appaction.dday.appActionDDayOverflowPolicyList
import io.github.taetae98coding.diary.work.appaction.dday.toAppActionDDay
import io.github.taetae98coding.diary.work.appaction.dday.toDDayOverflowPolicyOrNull
import io.github.taetae98coding.diary.work.appaction.dday.toDDayOverflowPolicyValue
import io.github.taetae98coding.diary.work.appaction.dday.toDDayRepeatInputListOrNull
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArgumentException
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArguments
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameter
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameterType
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

internal const val REPEATS = "repeats"
internal const val OVERFLOW_POLICY = "overflowPolicy"

internal val overflowPolicyParameter: McpToolParameter =
    McpToolParameter(
        name = OVERFLOW_POLICY,
        type = McpToolParameterType.STRING,
        description =
            "What to do when a monthly anniversary falls on a date the month does not have. " +
                "clamp_to_month_end uses the last day of the month, skip does not show it, next_month_first_day uses the 1st of the next month.",
        allowedValueList = appActionDDayOverflowPolicyList.map { policy -> policy.toDDayOverflowPolicyValue() },
    )

internal fun repeatsParameter(description: String): McpToolParameter =
    McpToolParameter(
        name = REPEATS,
        type = McpToolParameterType.STRING_LIST,
        description =
            "$description Each interval is an ISO 8601 period of days only or of years and months only, 1 or greater, like P100D (100 days), P3M (3 months) or P1Y (1 year). " +
                "Weeks and times are not accepted.",
    )

internal fun McpToolArguments.repeatInputListOrNull(): List<DDayRepeatInput>? {
    if (!has(REPEATS)) return null

    return stringList(REPEATS).toDDayRepeatInputListOrNull()
        ?: throw McpToolArgumentException(message = "Each '$REPEATS' item must be an ISO 8601 period of days only or of years and months only, 1 or greater, like P100D, P3M or P1Y.")
}

internal fun McpToolArguments.overflowPolicyOrNull(): DDayOverflowPolicy? {
    val value = stringOrNull(OVERFLOW_POLICY) ?: return null

    return value.toDDayOverflowPolicyOrNull()
        ?: throw McpToolArgumentException(message = "'$OVERFLOW_POLICY' must be one of ${appActionDDayOverflowPolicyList.joinToString { policy -> policy.toDDayOverflowPolicyValue() }}.")
}

internal fun DDay.toJson(today: LocalDate): JsonObject {
    val dday = toAppActionDDay(today = today)

    return buildJsonObject {
        put("id", dday.id)
        put("title", dday.title)
        put("description", dday.description)
        put("color", dday.color)
        put("baseDate", dday.baseDate)
        put("countsBaseDay", dday.countsBaseDay)
        put(REPEATS, JsonArray(dday.repeats.map(::JsonPrimitive)))
        put("endDate", dday.endDate)
        put(OVERFLOW_POLICY, dday.overflowPolicy)
        put("createdAt", dday.createdAt)
        put("updatedAt", dday.updatedAt)
        put("count", dday.count.toJson())
    }
}

private fun AppActionDDayCount.toJson(): JsonObject =
    buildJsonObject {
        put("status", status)
        remainingDays?.let { days -> put("remainingDays", days) }
        passedDays?.let { days -> put("passedDays", days) }
        years?.let { value -> put("years", value) }
        months?.let { value -> put("months", value) }
    }

internal fun CalendarDDay.toJson(): JsonObject {
    val dday = toAppActionCalendarDDay()

    return buildJsonObject {
        put("ddayId", dday.ddayId)
        put("title", dday.title)
        put("color", dday.color)
        put("date", dday.date)
        put(
            "label",
            buildJsonObject {
                put("kind", dday.label.kind)
                dday.label.count?.let { count -> put("count", count) }
            },
        )
    }
}
