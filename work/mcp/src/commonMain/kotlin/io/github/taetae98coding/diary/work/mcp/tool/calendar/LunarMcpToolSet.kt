package io.github.taetae98coding.diary.work.mcp.tool.calendar

import io.github.taetae98coding.diary.core.model.lunar.LunarDate
import io.github.taetae98coding.diary.domain.lunar.usecase.GetLunarUseCase
import io.github.taetae98coding.diary.work.mcp.tool.McpTool
import io.github.taetae98coding.diary.work.mcp.tool.McpToolKind
import io.github.taetae98coding.diary.work.mcp.tool.McpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.json.dateRange
import io.github.taetae98coding.diary.work.mcp.tool.json.dateRangeEndParameter
import io.github.taetae98coding.diary.work.mcp.tool.json.dateRangeStartParameter
import io.github.taetae98coding.diary.work.mcp.tool.json.toJsonArray
import io.github.taetae98coding.diary.work.mcp.tool.readFirst
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.koin.core.annotation.Factory

@Factory
internal class LunarMcpToolSet(
    private val getLunarUseCase: GetLunarUseCase,
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
                getLunarUseCase
                    .readFirst(parameter = arguments.dateRange())
                    .toJsonArray { lunarDate -> lunarDate.toJson() }
            },
        )
}

private fun LunarDate.toJson(): JsonObject =
    buildJsonObject {
        put("solarDate", solar.toString())
        put("lunarYear", year)
        put("lunarMonth", month)
        put("lunarDay", day)
        put("isLeapMonth", isLeapMonth)
    }
