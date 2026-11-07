package io.github.taetae98coding.diary.work.mcp.tool.json

import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArgumentException
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArguments
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameter
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameterType
import io.github.taetae98coding.diary.work.mcp.tool.localDate
import io.github.taetae98coding.diary.work.mcp.tool.localDateTimeOrNull
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

private const val ALL_DAY = "allDay"
private const val END_INCLUSIVE = "endInclusive"
private const val DATE_TIME_RANGE_MESSAGE = "'dateTime.endInclusive' must not be before 'dateTime.start'."
private const val START = "start"

internal const val DATE_RANGE_START = "startDate"
internal const val DATE_RANGE_END = "endDate"

internal val dateRangeStartParameter: McpToolParameter =
    McpToolParameter(name = DATE_RANGE_START, type = McpToolParameterType.STRING, description = "First date of the range, like 2026-01-01.", isRequired = true)

internal val dateRangeEndParameter: McpToolParameter =
    McpToolParameter(name = DATE_RANGE_END, type = McpToolParameterType.STRING, description = "Last date of the range, inclusive, like 2026-01-31.", isRequired = true)

internal fun McpToolArguments.dateRange(): LocalDateRange {
    val start = localDate(DATE_RANGE_START)
    val end = localDate(DATE_RANGE_END)

    if (end < start) throw McpToolArgumentException(message = "'$DATE_RANGE_END' must not be before '$DATE_RANGE_START'.")

    return start..end
}

internal fun LocalDateRange.toJson(): JsonObject =
    buildJsonObject {
        put(START, start.toString())
        put(END_INCLUSIVE, endInclusive.toString())
    }

internal val memoDateTimeParameter: McpToolParameter =
    McpToolParameter(
        name = "dateTime",
        type = McpToolParameterType.OBJECT,
        description =
            """Optional period. All-day: {"allDay": true, "start": "2026-01-01", "endInclusive": "2026-01-02"}. """ +
                """Timed: {"allDay": false, "start": "2026-01-01T09:00", "endInclusive": "2026-01-01T10:00"}.""",
    )

internal fun MemoDateTime.toJson(): JsonObject =
    when (this) {
        is MemoDateTime.AllDay ->
            buildJsonObject {
                put(ALL_DAY, true)
                put(START, dateRange.start.toString())
                put(END_INCLUSIVE, dateRange.endInclusive.toString())
            }

        is MemoDateTime.DateTime ->
            buildJsonObject {
                put(ALL_DAY, false)
                put(START, start.toString())
                put(END_INCLUSIVE, endInclusive.toString())
            }
    }

internal fun McpToolArguments.memoDateTimeOrNull(): MemoDateTime? {
    val jsonObject = objectOrNull("dateTime") ?: return null
    val arguments = McpToolArguments(jsonObject)
    val isAllDay = arguments.booleanOrNull(ALL_DAY) ?: throw McpToolArgumentException(message = "'dateTime.allDay' is required.")

    return if (isAllDay) arguments.allDayOrThrow() else arguments.timedOrThrow()
}

private fun McpToolArguments.allDayOrThrow(): MemoDateTime.AllDay {
    val start: LocalDate = localDate(START)
    val end: LocalDate = localDate(END_INCLUSIVE)
    if (end < start) throw McpToolArgumentException(message = DATE_TIME_RANGE_MESSAGE)

    return MemoDateTime.AllDay(dateRange = start..end)
}

private fun McpToolArguments.timedOrThrow(): MemoDateTime.DateTime {
    val start: LocalDateTime = requiredDateTime(START, "'dateTime.start' is required.")
    val end: LocalDateTime = requiredDateTime(END_INCLUSIVE, "'dateTime.endInclusive' is required.")
    if (end < start) throw McpToolArgumentException(message = DATE_TIME_RANGE_MESSAGE)

    return MemoDateTime.DateTime(start = start, endInclusive = end)
}

private fun McpToolArguments.requiredDateTime(
    name: String,
    message: String,
): LocalDateTime = localDateTimeOrNull(name) ?: throw McpToolArgumentException(message = message)
