package io.github.taetae98coding.diary.work.mcp.tool.search

import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.work.appaction.search.SearchAppAction
import io.github.taetae98coding.diary.work.mcp.tool.McpTool
import io.github.taetae98coding.diary.work.mcp.tool.McpToolKind
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameter
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameterType
import io.github.taetae98coding.diary.work.mcp.tool.McpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.appActionCall
import io.github.taetae98coding.diary.work.mcp.tool.cursorText
import io.github.taetae98coding.diary.work.mcp.tool.dday.toJson
import io.github.taetae98coding.diary.work.mcp.tool.json.sortParameter
import io.github.taetae98coding.diary.work.mcp.tool.json.toJson
import io.github.taetae98coding.diary.work.mcp.tool.paging.mcpPageCursorParameter
import io.github.taetae98coding.diary.work.mcp.tool.paging.toJson
import io.github.taetae98coding.diary.work.mcp.tool.routine.toJson
import io.github.taetae98coding.diary.work.mcp.tool.sortText
import org.koin.core.annotation.Factory

private const val QUERY = "query"

private val searchSortList: List<ListSort> = listOf(ListSort.TITLE, ListSort.RECENTLY_UPDATED)

private val queryParameter: McpToolParameter =
    McpToolParameter(
        name = QUERY,
        type = McpToolParameterType.STRING,
        description = "Search query. A blank query returns no results.",
        isRequired = true,
    )

private val searchParameterList: List<McpToolParameter> = listOf(queryParameter, sortParameter(searchSortList), mcpPageCursorParameter)

@Factory(binds = [McpToolSet::class])
internal class SearchMcpToolSet(
    private val searchAppAction: SearchAppAction,
) : McpToolSet {
    override val toolList: List<McpTool> =
        listOf(
            McpTool(
                name = "search_memo",
                description = "Searches memos that are not deleted, including finished ones, by the query, like the memo results of the search screen.",
                parameterList = searchParameterList,
                kind = McpToolKind.READ,
            ) { arguments ->
                appActionCall { searchAppAction.memo(query = arguments.string(QUERY), sort = arguments.sortText(searchSortList), cursor = arguments.cursorText()) }.toJson { memo -> memo.toJson() }
            },
            McpTool(
                name = "search_tag",
                description = "Searches tags by the query, like the tag results of the search screen.",
                parameterList = searchParameterList,
                kind = McpToolKind.READ,
            ) { arguments ->
                appActionCall { searchAppAction.tag(query = arguments.string(QUERY), sort = arguments.sortText(searchSortList), cursor = arguments.cursorText()) }.toJson { tag -> tag.toJson() }
            },
            McpTool(
                name = "search_place",
                description = "Searches saved places by the query, like the place results of the search screen. It does not search places outside the app.",
                parameterList = searchParameterList,
                kind = McpToolKind.READ,
            ) { arguments ->
                appActionCall { searchAppAction.place(query = arguments.string(QUERY), sort = arguments.sortText(searchSortList), cursor = arguments.cursorText()) }.toJson { place -> place.toJson() }
            },
            McpTool(
                name = "search_web",
                description = "Searches saved webs by the query, like the web results of the search screen.",
                parameterList = searchParameterList,
                kind = McpToolKind.READ,
            ) { arguments ->
                appActionCall { searchAppAction.web(query = arguments.string(QUERY), sort = arguments.sortText(searchSortList), cursor = arguments.cursorText()) }.toJson { web -> web.toJson() }
            },
            McpTool(
                name = "search_dday",
                description = "Searches saved D-Days by the query, like the D-Day results of the search screen, with the days left or passed counted from today.",
                parameterList = searchParameterList,
                kind = McpToolKind.READ,
            ) { arguments ->
                appActionCall { searchAppAction.dday(query = arguments.string(QUERY), sort = arguments.sortText(searchSortList), cursor = arguments.cursorText()) }.toJson { dday -> dday.toJson() }
            },
            McpTool(
                name = "search_routine",
                description =
                    "Searches saved routines by the query, like the routine results of the search screen, finished routines included, in progress first, then not started, by title within each, " +
                        "with this cycle's total and achievement rate counted from today.",
                parameterList = listOf(queryParameter, mcpPageCursorParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                appActionCall { searchAppAction.routine(query = arguments.string(QUERY), cursor = arguments.cursorText()) }.toJson { routine -> routine.toJson() }
            },
        )
}
