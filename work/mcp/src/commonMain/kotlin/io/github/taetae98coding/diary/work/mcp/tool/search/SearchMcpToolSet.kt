package io.github.taetae98coding.diary.work.mcp.tool.search

import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.domain.search.usecase.SearchMemoUseCase
import io.github.taetae98coding.diary.domain.search.usecase.SearchPlaceUseCase
import io.github.taetae98coding.diary.domain.search.usecase.SearchTagUseCase
import io.github.taetae98coding.diary.domain.search.usecase.SearchWebUseCase
import io.github.taetae98coding.diary.work.mcp.tool.McpTool
import io.github.taetae98coding.diary.work.mcp.tool.McpToolKind
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameter
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameterType
import io.github.taetae98coding.diary.work.mcp.tool.McpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.json.sort
import io.github.taetae98coding.diary.work.mcp.tool.json.sortParameter
import io.github.taetae98coding.diary.work.mcp.tool.json.toJson
import io.github.taetae98coding.diary.work.mcp.tool.paging.mcpPageCursorParameter
import io.github.taetae98coding.diary.work.mcp.tool.paging.pageOffset
import io.github.taetae98coding.diary.work.mcp.tool.paging.readPage
import io.github.taetae98coding.diary.work.mcp.tool.paging.toJson
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

@Factory
internal class SearchMcpToolSet(
    private val searchMemoUseCase: SearchMemoUseCase,
    private val searchTagUseCase: SearchTagUseCase,
    private val searchPlaceUseCase: SearchPlaceUseCase,
    private val searchWebUseCase: SearchWebUseCase,
) : McpToolSet {
    override val toolList: List<McpTool> =
        listOf(
            McpTool(
                name = "search_memo",
                description = "Searches memos that are not deleted, including finished ones, by the query, like the memo results of the search screen.",
                parameterList = searchParameterList,
                kind = McpToolKind.READ,
            ) { arguments ->
                searchMemoUseCase
                    .readPage(
                        parameter = SearchMemoUseCase.Parameter(query = arguments.string(QUERY), sort = arguments.sort(searchSortList)),
                        offset = arguments.pageOffset(),
                    ).toJson { memo -> memo.toJson() }
            },
            McpTool(
                name = "search_tag",
                description = "Searches tags by the query, like the tag results of the search screen.",
                parameterList = searchParameterList,
                kind = McpToolKind.READ,
            ) { arguments ->
                searchTagUseCase
                    .readPage(
                        parameter = SearchTagUseCase.Parameter(query = arguments.string(QUERY), sort = arguments.sort(searchSortList)),
                        offset = arguments.pageOffset(),
                    ).toJson { tag -> tag.toJson() }
            },
            McpTool(
                name = "search_place",
                description = "Searches saved places by the query, like the place results of the search screen. It does not search places outside the app.",
                parameterList = searchParameterList,
                kind = McpToolKind.READ,
            ) { arguments ->
                searchPlaceUseCase
                    .readPage(
                        parameter = SearchPlaceUseCase.Parameter(query = arguments.string(QUERY), sort = arguments.sort(searchSortList)),
                        offset = arguments.pageOffset(),
                    ).toJson { place -> place.toJson() }
            },
            McpTool(
                name = "search_web",
                description = "Searches saved webs by the query, like the web results of the search screen.",
                parameterList = searchParameterList,
                kind = McpToolKind.READ,
            ) { arguments ->
                searchWebUseCase
                    .readPage(
                        parameter = SearchWebUseCase.Parameter(query = arguments.string(QUERY), sort = arguments.sort(searchSortList)),
                        offset = arguments.pageOffset(),
                    ).toJson { web -> web.toJson() }
            },
        )
}
