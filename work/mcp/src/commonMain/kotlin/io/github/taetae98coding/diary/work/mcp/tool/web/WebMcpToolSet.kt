package io.github.taetae98coding.diary.work.mcp.tool.web

import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.web.WebDetail
import io.github.taetae98coding.diary.domain.memo.usecase.PageFinishedWebMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageWebMemoUseCase
import io.github.taetae98coding.diary.domain.tag.EntityTagKind
import io.github.taetae98coding.diary.domain.tag.usecase.AddEntityTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.GetEntityTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.RemoveEntityTagUseCase
import io.github.taetae98coding.diary.domain.web.usecase.AddWebUseCase
import io.github.taetae98coding.diary.domain.web.usecase.DeleteWebUseCase
import io.github.taetae98coding.diary.domain.web.usecase.FindWebUseCase
import io.github.taetae98coding.diary.domain.web.usecase.PageWebUseCase
import io.github.taetae98coding.diary.domain.web.usecase.RestoreWebUseCase
import io.github.taetae98coding.diary.domain.web.usecase.UpdateWebUseCase
import io.github.taetae98coding.diary.work.mcp.tool.McpTool
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArguments
import io.github.taetae98coding.diary.work.mcp.tool.McpToolKind
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameter
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameterType
import io.github.taetae98coding.diary.work.mcp.tool.McpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.json.changedCountResult
import io.github.taetae98coding.diary.work.mcp.tool.json.doneResult
import io.github.taetae98coding.diary.work.mcp.tool.json.idResult
import io.github.taetae98coding.diary.work.mcp.tool.json.sort
import io.github.taetae98coding.diary.work.mcp.tool.json.sortParameter
import io.github.taetae98coding.diary.work.mcp.tool.json.toJson
import io.github.taetae98coding.diary.work.mcp.tool.json.toJsonArray
import io.github.taetae98coding.diary.work.mcp.tool.memo.memoSortList
import io.github.taetae98coding.diary.work.mcp.tool.paging.mcpPageCursorParameter
import io.github.taetae98coding.diary.work.mcp.tool.paging.pageOffset
import io.github.taetae98coding.diary.work.mcp.tool.paging.readPage
import io.github.taetae98coding.diary.work.mcp.tool.paging.toJson
import io.github.taetae98coding.diary.work.mcp.tool.readFirst
import io.github.taetae98coding.diary.work.mcp.tool.uuid
import io.github.taetae98coding.diary.work.mcp.tool.uuidSet
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

private const val DESCRIPTION = "description"
private const val TITLE = "title"
private const val WEB_ID = "webId"

internal val webSortList: List<ListSort> = listOf(ListSort.TITLE, ListSort.RECENTLY_UPDATED)

internal val webIdParameter: McpToolParameter =
    McpToolParameter(name = WEB_ID, type = McpToolParameterType.STRING, description = "Id of the web.", isRequired = true)

@Factory
internal class WebMcpToolSet(
    private val pageWebUseCase: PageWebUseCase,
    private val findWebUseCase: FindWebUseCase,
    private val getEntityTagUseCase: GetEntityTagUseCase,
    private val pageWebMemoUseCase: PageWebMemoUseCase,
    private val pageFinishedWebMemoUseCase: PageFinishedWebMemoUseCase,
    private val addWebUseCase: AddWebUseCase,
    private val updateWebUseCase: UpdateWebUseCase,
    private val deleteWebUseCase: DeleteWebUseCase,
    private val restoreWebUseCase: RestoreWebUseCase,
    private val addEntityTagUseCase: AddEntityTagUseCase,
    private val removeEntityTagUseCase: RemoveEntityTagUseCase,
) : McpToolSet {
    override val toolList: List<McpTool> =
        listOf(
            McpTool(
                name = "web_list",
                description = "Lists all webs (saved web pages) that are not deleted, as on the web home list.",
                parameterList = listOf(sortParameter(webSortList), mcpPageCursorParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                pageWebUseCase
                    .readPage(parameter = arguments.sort(webSortList), offset = arguments.pageOffset())
                    .toJson { web -> web.toJson() }
            },
            McpTool(
                name = "web_get",
                description = "Gets one web with its request headers and linked tags. Returns null when the web does not exist.",
                parameterList = listOf(webIdParameter),
                kind = McpToolKind.READ,
            ) { arguments -> getWeb(arguments) },
            McpTool(
                name = "web_memo_list",
                description = "Lists the memos linked to a web that are neither finished nor deleted.",
                parameterList = listOf(webIdParameter, sortParameter(memoSortList), mcpPageCursorParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                pageWebMemoUseCase
                    .readPage(
                        parameter = PageWebMemoUseCase.Parameter(webId = arguments.uuid(WEB_ID), sort = arguments.sort(memoSortList)),
                        offset = arguments.pageOffset(),
                    ).toJson { memo -> memo.toJson() }
            },
            McpTool(
                name = "web_memo_finished_list",
                description = "Lists the finished memos linked to a web that are not deleted.",
                parameterList = listOf(webIdParameter, sortParameter(memoSortList), mcpPageCursorParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                pageFinishedWebMemoUseCase
                    .readPage(
                        parameter = PageFinishedWebMemoUseCase.Parameter(webId = arguments.uuid(WEB_ID), sort = arguments.sort(memoSortList)),
                        offset = arguments.pageOffset(),
                    ).toJson { memo -> memo.toJson() }
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
                val id =
                    addWebUseCase(
                        parameter =
                            AddWebUseCase.Parameter(
                                detail =
                                    WebDetail(
                                        title = arguments.string(TITLE),
                                        description = arguments.stringOrNull(DESCRIPTION).orEmpty(),
                                        url = arguments.string("url"),
                                        headerList = arguments.headerListOrNull().orEmpty(),
                                    ),
                                tagIdSet = arguments.uuidSet("tagIds"),
                            ),
                    ).getOrThrow()

                idResult(id)
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
            ) { arguments -> changedCountResult(deleteWebUseCase(parameter = arguments.uuid(WEB_ID)).getOrThrow()) },
            McpTool(
                name = "web_restore",
                description = "Restores a deleted web.",
                parameterList = listOf(webIdParameter),
                kind = McpToolKind.WRITE,
            ) { arguments -> changedCountResult(restoreWebUseCase(parameter = arguments.uuid(WEB_ID)).getOrThrow()) },
            tagLinkTool(name = "web_tag_add", description = "Links a tag to a web.") { webId, tagId ->
                addEntityTagUseCase(parameter = AddEntityTagUseCase.Parameter(kind = EntityTagKind.WEB, entityId = webId, tagId = tagId)).getOrThrow()
            },
            tagLinkTool(name = "web_tag_remove", description = "Unlinks a tag from a web.") { webId, tagId ->
                removeEntityTagUseCase(parameter = RemoveEntityTagUseCase.Parameter(kind = EntityTagKind.WEB, entityId = webId, tagId = tagId)).getOrThrow()
            },
        )

    private suspend fun getWeb(arguments: McpToolArguments) =
        run {
            val webId = arguments.uuid(WEB_ID)
            val web = findWebUseCase.readFirst(parameter = webId) ?: return@run JsonNull

            buildJsonObject {
                put("web", web.toDetailJson())
                put("tags", getEntityTagUseCase.readFirst(parameter = GetEntityTagUseCase.Parameter(kind = EntityTagKind.WEB, entityId = webId)).toJsonArray { tag -> tag.toJson() })
            }
        }

    private suspend fun updateWeb(arguments: McpToolArguments) =
        run {
            val webId = arguments.uuid(WEB_ID)
            val current = findWebUseCase.readFirst(parameter = webId)?.detail ?: return@run changedCountResult(count = 0)
            val detail =
                WebDetail(
                    title = arguments.stringOrNull(TITLE) ?: current.title,
                    description = arguments.stringOrNull(DESCRIPTION) ?: current.description,
                    url = arguments.stringOrNull("url") ?: current.url,
                    headerList = arguments.headerListOrNull() ?: current.headerList,
                )

            changedCountResult(updateWebUseCase(parameter = UpdateWebUseCase.Parameter(id = webId, detail = detail)).getOrThrow())
        }

    private fun tagLinkTool(
        name: String,
        description: String,
        link: suspend (webId: Uuid, tagId: Uuid) -> Unit,
    ): McpTool =
        McpTool(
            name = name,
            description = description,
            parameterList =
                listOf(
                    webIdParameter,
                    McpToolParameter(name = "tagId", type = McpToolParameterType.STRING, description = "Id of the tag.", isRequired = true),
                ),
            kind = McpToolKind.WRITE,
        ) { arguments: McpToolArguments ->
            link(arguments.uuid(WEB_ID), arguments.uuid("tagId"))
            doneResult as JsonElement
        }
}
