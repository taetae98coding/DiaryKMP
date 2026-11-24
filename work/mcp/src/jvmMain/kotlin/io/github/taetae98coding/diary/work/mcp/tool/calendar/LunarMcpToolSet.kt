package io.github.taetae98coding.diary.work.mcp.tool.calendar

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import io.github.taetae98coding.diary.work.appaction.lunar.AppActionLunarDate
import io.github.taetae98coding.diary.work.appaction.lunar.LunarAppAction
import io.github.taetae98coding.diary.work.mcp.tool.McpTool
import io.github.taetae98coding.diary.work.mcp.tool.McpToolKind
import io.github.taetae98coding.diary.work.mcp.tool.McpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.appActionCall
import io.github.taetae98coding.diary.work.mcp.tool.json.dateRange
import io.github.taetae98coding.diary.work.mcp.tool.json.dateRangeEndParameter
import io.github.taetae98coding.diary.work.mcp.tool.json.dateRangeStartParameter
import io.github.taetae98coding.diary.work.mcp.tool.json.toJsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Inject
@ContributesIntoSet(AppScope::class, binding = binding<McpToolSet>())
internal class LunarMcpToolSet(
    private val lunarAppAction: LunarAppAction,
) : McpToolSet {
    override val toolList: List<McpTool> =
        listOf(
            McpTool(
                name = "lunar_list",
                description =
                    "Lists the lunar dates of solar dates in a range, from the lunar data saved on this device. " +
                        "Dates whose lunar data has not been downloaded yet are missing from the result.",
                parameterList = listOf(dateRangeStartParameter, dateRangeEndParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                val dateRange = arguments.dateRange()

                appActionCall { lunarAppAction.list(startDate = dateRange.start.toString(), endDate = dateRange.endInclusive.toString()) }.toJsonArray { lunarDate -> lunarDate.toJson() }
            },
        )
}

private fun AppActionLunarDate.toJson(): JsonObject =
    buildJsonObject {
        put("solarDate", solarDate)
        put("lunarYear", lunarYear)
        put("lunarMonth", lunarMonth)
        put("lunarDay", lunarDay)
        put("isLeapMonth", isLeapMonth)
    }
