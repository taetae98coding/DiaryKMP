package io.github.taetae98coding.diary.work.mcp.tool.web

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.core.model.web.WebDetail
import io.github.taetae98coding.diary.domain.web.usecase.FindWebUseCase
import io.github.taetae98coding.diary.domain.web.usecase.UpdateWebUseCase
import io.github.taetae98coding.diary.work.appaction.core.readActive
import io.github.taetae98coding.diary.work.appaction.memo.MemoListAppAction
import io.github.taetae98coding.diary.work.appaction.tag.AppActionEntityTagKind
import io.github.taetae98coding.diary.work.appaction.tag.EntityTagAppAction
import io.github.taetae98coding.diary.work.appaction.web.WebAppAction
import io.github.taetae98coding.diary.work.mcp.tool.McpTool
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArguments
import io.github.taetae98coding.diary.work.mcp.tool.McpToolKind
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameter
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameterType
import io.github.taetae98coding.diary.work.mcp.tool.McpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.appActionCall
import io.github.taetae98coding.diary.work.mcp.tool.cursorText
import io.github.taetae98coding.diary.work.mcp.tool.idText
import io.github.taetae98coding.diary.work.mcp.tool.idTextList
import io.github.taetae98coding.diary.work.mcp.tool.json.changedCountResult
import io.github.taetae98coding.diary.work.mcp.tool.json.sortParameter
import io.github.taetae98coding.diary.work.mcp.tool.json.toJson
import io.github.taetae98coding.diary.work.mcp.tool.memo.memoSortList
import io.github.taetae98coding.diary.work.mcp.tool.paging.mcpPageCursorParameter
import io.github.taetae98coding.diary.work.mcp.tool.paging.toJson
import io.github.taetae98coding.diary.work.mcp.tool.sortText
import io.github.taetae98coding.diary.work.mcp.tool.tag.entityTagToolList
import io.github.taetae98coding.diary.work.mcp.tool.uuid
import kotlinx.serialization.json.JsonNull

private const val DESCRIPTION = "description"
private const val TITLE = "title"
private const val WEB_ID = "webId"

internal val webSortList: List<ListSort> = listOf(ListSort.TITLE, ListSort.RECENTLY_UPDATED)

internal val webIdParameter: McpToolParameter =
    McpToolParameter(name = WEB_ID, type = McpToolParameterType.STRING, description = "Id of the web.", isRequired = true)

@Inject
@ContributesIntoSet(AppScope::class, binding = binding<McpToolSet>())
internal class WebMcpToolSet(
    private val webAppAction: WebAppAction,
    private val memoListAppAction: MemoListAppAction,
    private val entityTagAppAction: EntityTagAppAction,
    private val findWebUseCase: FindWebUseCase,
    private val updateWebUseCase: UpdateWebUseCase,
) : McpToolSet {
    override val toolList: List<McpTool> =
        listOf(
            McpTool(
                name = "web_list",
                description = "Lists all webs (saved web pages) that are not deleted, as on the web home list.",
                parameterList = listOf(sortParameter(webSortList), mcpPageCursorParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                appActionCall { webAppAction.list(sort = arguments.sortText(webSortList), cursor = arguments.cursorText()) }.toJson { web -> web.toJson() }
            },
            McpTool(
                name = "web_get",
                description = "Gets one web with its request headers and linked tags. Returns null when the web does not exist or is deleted.",
                parameterList = listOf(webIdParameter),
                kind = McpToolKind.READ,
            ) { arguments -> appActionCall { webAppAction.get(webId = arguments.idText(WEB_ID)) }?.toJson() ?: JsonNull },
            McpTool(
                name = "web_memo_list",
                description = "Lists the memos linked to a web that are neither finished nor deleted.",
                parameterList = listOf(webIdParameter, sortParameter(memoSortList), mcpPageCursorParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                appActionCall {
                    memoListAppAction.webList(webId = arguments.idText(WEB_ID), sort = arguments.sortText(memoSortList), cursor = arguments.cursorText())
                }.toJson { memo -> memo.toJson() }
            },
            McpTool(
                name = "web_memo_finished_list",
                description = "Lists the finished memos linked to a web that are not deleted.",
                parameterList = listOf(webIdParameter, sortParameter(memoSortList), mcpPageCursorParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                appActionCall {
                    memoListAppAction.webFinishedList(webId = arguments.idText(WEB_ID), sort = arguments.sortText(memoSortList), cursor = arguments.cursorText())
                }.toJson { memo -> memo.toJson() }
            },
            McpTool(
                name = "web_add",
                description =
                    "Adds a web. The title and url must not be blank and every header needs a name. " +
                        "The page is not fetched, so pass the title you already know.",
                parameterList =
                    listOf(
                        McpToolParameter(name = TITLE, type = McpToolParameterType.STRING, description = "Title of the web.", isRequired = true),
                        McpToolParameter(name = "url", type = McpToolParameterType.STRING, description = "Url of the web page.", isRequired = true),
                        McpToolParameter(name = DESCRIPTION, type = McpToolParameterType.STRING, description = "Description of the web."),
                        headersParameter(description = "Request headers sent when the page is opened in the app."),
                        McpToolParameter(name = "tagIds", type = McpToolParameterType.STRING_LIST, description = "Ids of tags to link."),
                    ),
                kind = McpToolKind.WRITE,
            ) { arguments ->
                appActionCall {
                    webAppAction.add(
                        title = arguments.string(TITLE),
                        description = arguments.stringOrNull(DESCRIPTION),
                        url = arguments.string("url"),
                        headers = arguments.headerTextList(),
                        tagIds = arguments.idTextList("tagIds"),
                    )
                }.toJson()
            },
            McpTool(
                name = "web_update",
                description =
                    "Updates the title, description, url or request headers of a web. Omitted fields keep their current value. " +
                        "A blank title or url keeps the current value. Tags are changed with web_tag_add and web_tag_remove.",
                parameterList =
                    listOf(
                        webIdParameter,
                        McpToolParameter(name = TITLE, type = McpToolParameterType.STRING, description = "New title."),
                        McpToolParameter(name = DESCRIPTION, type = McpToolParameterType.STRING, description = "New description."),
                        McpToolParameter(name = "url", type = McpToolParameterType.STRING, description = "New url."),
                        headersParameter(description = "New request headers. They replace all current headers; pass an empty list to remove them."),
                    ),
                kind = McpToolKind.WRITE,
            ) { arguments -> updateWeb(arguments) },
            McpTool(
                name = "web_delete",
                description = "Deletes a web. Links to memos are kept. Use web_restore with the same id to undo.",
                parameterList = listOf(webIdParameter),
                kind = McpToolKind.DESTRUCTIVE,
            ) { arguments -> appActionCall { webAppAction.delete(webId = arguments.idText(WEB_ID)) }.toJson() },
            McpTool(
                name = "web_restore",
                description = "Restores a deleted web.",
                parameterList = listOf(webIdParameter),
                kind = McpToolKind.WRITE,
            ) { arguments -> appActionCall { webAppAction.restore(webId = arguments.idText(WEB_ID)) }.toJson() },
        ) +
            entityTagToolList(
                toolNamePrefix = "web",
                entityName = "a web",
                kind = AppActionEntityTagKind.WEB,
                entityIdParameter = webIdParameter,
                entityTagAppAction = entityTagAppAction,
            )

    private suspend fun updateWeb(arguments: McpToolArguments) =
        run {
            val webId = arguments.uuid(WEB_ID)
            val current = findWebUseCase.readActive(id = webId, isDeleted = Web::isDeleted)?.detail ?: return@run changedCountResult(count = 0)
            val detail =
                WebDetail(
                    title = arguments.stringOrNull(TITLE) ?: current.title,
                    description = arguments.stringOrNull(DESCRIPTION) ?: current.description,
                    url = arguments.stringOrNull("url") ?: current.url,
                    headerList = arguments.headerListOrNull() ?: current.headerList,
                )

            changedCountResult(updateWebUseCase(parameter = UpdateWebUseCase.Parameter(id = webId, detail = detail)).getOrThrow())
        }
}
