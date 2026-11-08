package io.github.taetae98coding.diary.work.mcp.tool.calendar

import io.github.taetae98coding.diary.core.model.contact.CalendarContactBirthday
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.core.model.memo.CalendarMemo
import io.github.taetae98coding.diary.domain.contact.usecase.GetCalendarContactBirthdayUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.GetCalendarDDayUseCase
import io.github.taetae98coding.diary.domain.holiday.usecase.GetCalendarHolidayUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.GetCalendarMemoUseCase
import io.github.taetae98coding.diary.work.mcp.tool.McpTool
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArgumentException
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArguments
import io.github.taetae98coding.diary.work.mcp.tool.McpToolKind
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameter
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameterType
import io.github.taetae98coding.diary.work.mcp.tool.McpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.dday.toJson
import io.github.taetae98coding.diary.work.mcp.tool.json.dateRange
import io.github.taetae98coding.diary.work.mcp.tool.json.dateRangeEndParameter
import io.github.taetae98coding.diary.work.mcp.tool.json.dateRangeStartParameter
import io.github.taetae98coding.diary.work.mcp.tool.json.put
import io.github.taetae98coding.diary.work.mcp.tool.json.toColorHex
import io.github.taetae98coding.diary.work.mcp.tool.json.toJson
import io.github.taetae98coding.diary.work.mcp.tool.json.toJsonArray
import io.github.taetae98coding.diary.work.mcp.tool.readFirst
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.koin.core.annotation.Factory

private const val YEAR = "year"

private val yearParameter: McpToolParameter =
    McpToolParameter(name = YEAR, type = McpToolParameterType.INTEGER, description = "Year like 2026.", isRequired = true)

@Factory
internal class CalendarMcpToolSet(
    private val getCalendarMemoUseCase: GetCalendarMemoUseCase,
    private val getCalendarContactBirthdayUseCase: GetCalendarContactBirthdayUseCase,
    private val getCalendarHolidayUseCase: GetCalendarHolidayUseCase,
    private val getCalendarDDayUseCase: GetCalendarDDayUseCase,
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
            ) { arguments -> getCalendarMemoUseCase.readFirst(parameter = arguments.dateRange()).toJsonArray { memo -> memo.toJson() } },
            McpTool(
                name = "calendar_birthday_list",
                description =
                    "Lists contact birthdays shown on the calendar within the date range, as solar dates. " +
                        "Lunar birthdays appear only for years whose lunar data is saved on this device.",
                parameterList = listOf(dateRangeStartParameter, dateRangeEndParameter),
                kind = McpToolKind.READ,
            ) { arguments -> getCalendarContactBirthdayUseCase.readFirst(parameter = arguments.dateRange()).toJsonArray { birthday -> birthday.toJson() } },
            McpTool(
                name = "calendar_dday_list",
                description =
                    "Lists the base dates and anniversaries of D-Days shown on the calendar within the date range, by date. " +
                        "Each label tells whether it is the base day or which count of days, months or years it is. The calendar tag filter the user chose in the app is applied.",
                parameterList = listOf(dateRangeStartParameter, dateRangeEndParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                getCalendarDDayUseCase
                    .readFirst(parameter = arguments.dateRange())
                    .sortedBy { dday -> dday.date }
                    .toJsonArray { dday -> dday.toJson() }
            },
            McpTool(
                name = "holiday_list",
                description =
                    "Lists the holidays shown on the calendar for a year, for the holiday countries the user chose and without the holidays the user hid. " +
                        "isHoliday is false for named days that are not days off. Only years already saved on this device are returned.",
                parameterList = listOf(yearParameter),
                kind = McpToolKind.READ,
            ) { arguments -> getCalendarHolidayUseCase.readFirst(parameter = arguments.year()).toJsonArray { holiday -> holiday.toJson() } },
        )
}

private fun McpToolArguments.year(): Int = intOrNull(YEAR) ?: throw McpToolArgumentException(message = "'$YEAR' is required.")

private fun CalendarMemo.toJson(): JsonObject =
    buildJsonObject {
        put("id", id)
        put("title", title)
        put("color", color.toColorHex())
        put("dateTime", dateTime.toJson())
    }

private fun CalendarContactBirthday.toJson(): JsonObject =
    buildJsonObject {
        put("contactId", contactId)
        put("name", name)
        put("nameQualifier", nameQualifier)
        put("date", date.toString())
    }

private fun Holiday.toJson(): JsonObject =
    buildJsonObject {
        put("name", name)
        put("isHoliday", isHoliday)
        put("start", dateRange.start.toString())
        put("endInclusive", dateRange.endInclusive.toString())
    }
