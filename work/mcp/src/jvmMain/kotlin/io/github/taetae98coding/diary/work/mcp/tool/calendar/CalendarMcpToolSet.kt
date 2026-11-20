package io.github.taetae98coding.diary.work.mcp.tool.calendar

import io.github.taetae98coding.diary.work.appaction.calendar.AppActionCalendarBirthday
import io.github.taetae98coding.diary.work.appaction.calendar.AppActionCalendarDDay
import io.github.taetae98coding.diary.work.appaction.calendar.AppActionCalendarMemo
import io.github.taetae98coding.diary.work.appaction.calendar.AppActionHoliday
import io.github.taetae98coding.diary.work.appaction.calendar.CalendarAppAction
import io.github.taetae98coding.diary.work.mcp.tool.McpTool
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArgumentException
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArguments
import io.github.taetae98coding.diary.work.mcp.tool.McpToolKind
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameter
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameterType
import io.github.taetae98coding.diary.work.mcp.tool.McpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.appActionCall
import io.github.taetae98coding.diary.work.mcp.tool.json.dateRange
import io.github.taetae98coding.diary.work.mcp.tool.json.dateRangeEndParameter
import io.github.taetae98coding.diary.work.mcp.tool.json.dateRangeStartParameter
import io.github.taetae98coding.diary.work.mcp.tool.json.toJson
import io.github.taetae98coding.diary.work.mcp.tool.json.toJsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.koin.core.annotation.Factory

private const val YEAR = "year"

private val yearParameter: McpToolParameter =
    McpToolParameter(name = YEAR, type = McpToolParameterType.INTEGER, description = "Year like 2026.", isRequired = true)

@Factory(binds = [McpToolSet::class])
internal class CalendarMcpToolSet(
    private val calendarAppAction: CalendarAppAction,
) : McpToolSet {
    override val toolList: List<McpTool> =
        listOf(
            McpTool(
                name = "calendar_memo_list",
                description =
                    "Lists the memos shown on the calendar whose period overlaps the date range. " +
                        "Finished memos are included. The calendar tag filter the user chose in the app is applied.",
                parameterList = listOf(dateRangeStartParameter, dateRangeEndParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                val dateRange = arguments.dateRange()

                appActionCall { calendarAppAction.memoList(startDate = dateRange.start.toString(), endDate = dateRange.endInclusive.toString()) }.toJsonArray { memo -> memo.toJson() }
            },
            McpTool(
                name = "calendar_birthday_list",
                description =
                    "Lists contact birthdays shown on the calendar within the date range, as solar dates. " +
                        "Lunar birthdays appear only for years whose lunar data is saved on this device.",
                parameterList = listOf(dateRangeStartParameter, dateRangeEndParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                val dateRange = arguments.dateRange()

                appActionCall { calendarAppAction.birthdayList(startDate = dateRange.start.toString(), endDate = dateRange.endInclusive.toString()) }.toJsonArray { birthday -> birthday.toJson() }
            },
            McpTool(
                name = "calendar_dday_list",
                description =
                    "Lists the base dates and anniversaries of D-Days shown on the calendar within the date range, by date. " +
                        "Each label tells whether it is the base day or which count of days, months or years it is. The calendar tag filter the user chose in the app is applied.",
                parameterList = listOf(dateRangeStartParameter, dateRangeEndParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                val dateRange = arguments.dateRange()

                appActionCall { calendarAppAction.ddayList(startDate = dateRange.start.toString(), endDate = dateRange.endInclusive.toString()) }.toJsonArray { dday -> dday.toJson() }
            },
            McpTool(
                name = "holiday_list",
                description =
                    "Lists the holidays shown on the calendar for a year, for the holiday countries the user chose and without the holidays the user hid. " +
                        "isHoliday is false for named days that are not days off. Only years already saved on this device are returned.",
                parameterList = listOf(yearParameter),
                kind = McpToolKind.READ,
            ) { arguments -> appActionCall { calendarAppAction.holidayList(year = arguments.year()) }.toJsonArray { holiday -> holiday.toJson() } },
        )
}

private fun McpToolArguments.year(): Int = intOrNull(YEAR) ?: throw McpToolArgumentException(message = "'$YEAR' is required.")

private fun AppActionCalendarMemo.toJson(): JsonObject =
    buildJsonObject {
        put("id", id)
        put("title", title)
        put("color", color)
        put("dateTime", dateTime.toJson())
    }

private fun AppActionCalendarBirthday.toJson(): JsonObject =
    buildJsonObject {
        put("contactId", contactId)
        put("name", name)
        put("nameQualifier", nameQualifier)
        put("date", date)
    }

private fun AppActionCalendarDDay.toJson(): JsonObject =
    buildJsonObject {
        put("ddayId", ddayId)
        put("title", title)
        put("color", color)
        put("date", date)
        put(
            "label",
            buildJsonObject {
                put("kind", label.kind)
                label.count?.let { count -> put("count", count) }
            },
        )
    }

private fun AppActionHoliday.toJson(): JsonObject =
    buildJsonObject {
        put("name", name)
        put("isHoliday", isHoliday)
        put("start", start)
        put("endInclusive", endInclusive)
    }
