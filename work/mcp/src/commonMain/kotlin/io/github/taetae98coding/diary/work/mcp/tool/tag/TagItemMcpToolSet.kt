package io.github.taetae98coding.diary.work.mcp.tool.tag

import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.domain.memo.usecase.PageFinishedTagMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageTagMemoUseCase
import io.github.taetae98coding.diary.domain.place.usecase.PageTagPlaceUseCase
import io.github.taetae98coding.diary.domain.web.usecase.PageTagWebUseCase
import io.github.taetae98coding.diary.work.mcp.tool.McpTool
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArgumentException
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArguments
import io.github.taetae98coding.diary.work.mcp.tool.McpToolKind
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameter
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameterType
import io.github.taetae98coding.diary.work.mcp.tool.McpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.json.sort
import io.github.taetae98coding.diary.work.mcp.tool.json.sortParameter
import io.github.taetae98coding.diary.work.mcp.tool.json.toJson
import io.github.taetae98coding.diary.work.mcp.tool.memo.memoSortList
import io.github.taetae98coding.diary.work.mcp.tool.paging.mcpPageCursorParameter
import io.github.taetae98coding.diary.work.mcp.tool.paging.pageOffset
import io.github.taetae98coding.diary.work.mcp.tool.paging.readPage
import io.github.taetae98coding.diary.work.mcp.tool.paging.toJson
import io.github.taetae98coding.diary.work.mcp.tool.uuid
import org.koin.core.annotation.Factory

private const val TAG_ID = "tagId"

private const val SCOPE = "scope"

internal val tagScopeList: List<TagScope> = listOf(TagScope.SELF, TagScope.CHILD, TagScope.DESCENDANT)

private fun TagScope.toArgument(): String =
    when (this) {
        TagScope.SELF -> "self"
        TagScope.CHILD -> "child"
        TagScope.DESCENDANT -> "descendant"
    }

private val scopeParameter: McpToolParameter =
    McpToolParameter(
        name = SCOPE,
        type = McpToolParameterType.STRING,
        description =
            "Which tags to include. self: only this tag. child: this tag and the tags it links to directly. " +
                "descendant: this tag and every tag reachable through its links. Omit it to use self.",
        allowedValueList = tagScopeList.map { scope -> scope.toArgument() },
    )

private fun McpToolArguments.scope(): TagScope {
    val argument = stringOrNull(SCOPE) ?: return TagScope.SELF

    return TagScope.entries.firstOrNull { scope -> scope.toArgument() == argument }
        ?: throw McpToolArgumentException(message = "'$SCOPE' must be one of ${tagScopeList.joinToString { scope -> scope.toArgument() }}.")
}

@Factory
internal class TagItemMcpToolSet(
    private val pageTagMemoUseCase: PageTagMemoUseCase,
    private val pageFinishedTagMemoUseCase: PageFinishedTagMemoUseCase,
    private val pageTagWebUseCase: PageTagWebUseCase,
    private val pageTagPlaceUseCase: PageTagPlaceUseCase,
) : McpToolSet {
    override val toolList: List<McpTool> =
        listOf(
            McpTool(
                name = "tag_memo_list",
                description = "Lists memos linked to a tag that are neither finished nor deleted. A memo linked to several included tags appears once.",
                parameterList = listOf(tagIdParameter, scopeParameter, sortParameter(memoSortList), mcpPageCursorParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                pageTagMemoUseCase
                    .readPage(
                        parameter = PageTagMemoUseCase.Parameter(tagId = arguments.uuid(TAG_ID), scope = arguments.scope(), sort = arguments.sort(memoSortList)),
                        offset = arguments.pageOffset(),
                    ).toJson { memo -> memo.toJson() }
            },
            McpTool(
                name = "tag_memo_finished_list",
                description = "Lists finished memos linked to a tag that are not deleted. Only memos linked to this tag itself are included.",
                parameterList = listOf(tagIdParameter, sortParameter(memoSortList), mcpPageCursorParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                pageFinishedTagMemoUseCase
                    .readPage(
                        parameter = PageFinishedTagMemoUseCase.Parameter(tagId = arguments.uuid(TAG_ID), sort = arguments.sort(memoSortList)),
                        offset = arguments.pageOffset(),
                    ).toJson { memo -> memo.toJson() }
            },
            McpTool(
                name = "tag_web_list",
                description = "Lists webs linked to a tag that are not deleted. A web linked to several included tags appears once.",
                parameterList = listOf(tagIdParameter, scopeParameter, sortParameter(tagSortList), mcpPageCursorParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                pageTagWebUseCase
                    .readPage(
                        parameter = PageTagWebUseCase.Parameter(tagId = arguments.uuid(TAG_ID), scope = arguments.scope(), sort = arguments.sort(tagSortList)),
                        offset = arguments.pageOffset(),
                    ).toJson { web -> web.toJson() }
            },
            McpTool(
                name = "tag_place_list",
                description = "Lists places linked to a tag that are not deleted. A place linked to several included tags appears once.",
                parameterList = listOf(tagIdParameter, scopeParameter, sortParameter(tagSortList), mcpPageCursorParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                pageTagPlaceUseCase
                    .readPage(
                        parameter = PageTagPlaceUseCase.Parameter(tagId = arguments.uuid(TAG_ID), scope = arguments.scope(), sort = arguments.sort(tagSortList)),
                        offset = arguments.pageOffset(),
                    ).toJson { place -> place.toJson() }
            },
        )
}
