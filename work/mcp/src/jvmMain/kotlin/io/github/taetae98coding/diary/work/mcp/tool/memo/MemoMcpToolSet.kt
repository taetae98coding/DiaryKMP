package io.github.taetae98coding.diary.work.mcp.tool.memo

import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.domain.memo.usecase.FindMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.UpdateMemoUseCase
import io.github.taetae98coding.diary.work.appaction.core.readActive
import io.github.taetae98coding.diary.work.appaction.memo.MemoAppAction
import io.github.taetae98coding.diary.work.appaction.memo.MemoListAppAction
import io.github.taetae98coding.diary.work.mcp.tool.McpTool
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArguments
import io.github.taetae98coding.diary.work.mcp.tool.McpToolKind
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameter
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameterType
import io.github.taetae98coding.diary.work.mcp.tool.McpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.appActionCall
import io.github.taetae98coding.diary.work.mcp.tool.colorTextOrNull
import io.github.taetae98coding.diary.work.mcp.tool.cursorText
import io.github.taetae98coding.diary.work.mcp.tool.idText
import io.github.taetae98coding.diary.work.mcp.tool.idTextList
import io.github.taetae98coding.diary.work.mcp.tool.idTextOrNull
import io.github.taetae98coding.diary.work.mcp.tool.json.changedCountResult
import io.github.taetae98coding.diary.work.mcp.tool.json.colorOrNull
import io.github.taetae98coding.diary.work.mcp.tool.json.colorParameter
import io.github.taetae98coding.diary.work.mcp.tool.json.memoDateTimeInputOrNull
import io.github.taetae98coding.diary.work.mcp.tool.json.memoDateTimeOrNull
import io.github.taetae98coding.diary.work.mcp.tool.json.memoDateTimeParameter
import io.github.taetae98coding.diary.work.mcp.tool.json.sortParameter
import io.github.taetae98coding.diary.work.mcp.tool.json.toJson
import io.github.taetae98coding.diary.work.mcp.tool.paging.mcpPageCursorParameter
import io.github.taetae98coding.diary.work.mcp.tool.paging.toJson
import io.github.taetae98coding.diary.work.mcp.tool.sortText
import io.github.taetae98coding.diary.work.mcp.tool.uuid
import kotlinx.serialization.json.JsonNull
import org.koin.core.annotation.Factory

private const val DESCRIPTION = "description"
private const val MEMO_ID = "memoId"
private const val TITLE = "title"

internal val memoSortList: List<ListSort> = listOf(ListSort.DEFAULT, ListSort.TITLE, ListSort.RECENTLY_UPDATED)

internal val memoIdParameter: McpToolParameter =
    McpToolParameter(name = MEMO_ID, type = McpToolParameterType.STRING, description = "Id of the memo.", isRequired = true)

@Factory(binds = [McpToolSet::class])
internal class MemoMcpToolSet(
    private val memoListAppAction: MemoListAppAction,
    private val memoAppAction: MemoAppAction,
    private val findMemoUseCase: FindMemoUseCase,
    private val updateMemoUseCase: UpdateMemoUseCase,
) : McpToolSet {
    override val toolList: List<McpTool> =
        listOf(
            McpTool(
                name = "memo_list",
                description = "Lists the memos shown on the memo home list: memos that are neither finished nor deleted, filtered by the filters the user chose in the app.",
                parameterList = listOf(sortParameter(memoSortList), mcpPageCursorParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                appActionCall { memoListAppAction.list(sort = arguments.sortText(memoSortList), cursor = arguments.cursorText()) }.toJson { memo -> memo.toJson() }
            },
            McpTool(
                name = "memo_finished_list",
                description = "Lists finished memos that are not deleted.",
                parameterList = listOf(sortParameter(memoSortList), mcpPageCursorParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                appActionCall { memoListAppAction.finishedList(sort = arguments.sortText(memoSortList), cursor = arguments.cursorText()) }.toJson { memo -> memo.toJson() }
            },
            McpTool(
                name = "memo_get",
                description = "Gets one memo with its linked tags, places, webs and contacts. Returns null when the memo does not exist or is deleted.",
                parameterList = listOf(memoIdParameter),
                kind = McpToolKind.READ,
            ) { arguments -> appActionCall { memoAppAction.get(memoId = arguments.idText(MEMO_ID)) }?.toJson() ?: JsonNull },
            McpTool(
                name = "memo_add",
                description = "Adds a memo. The title must not be blank.",
                parameterList =
                    listOf(
                        McpToolParameter(name = TITLE, type = McpToolParameterType.STRING, description = "Title of the memo.", isRequired = true),
                        McpToolParameter(name = DESCRIPTION, type = McpToolParameterType.STRING, description = "Description of the memo."),
                        colorParameter(description = "Color of the memo. A random color is used when omitted."),
                        memoDateTimeParameter,
                        McpToolParameter(name = "primaryTagId", type = McpToolParameterType.STRING, description = "Id of the primary tag. It is linked too."),
                        McpToolParameter(name = "tagIds", type = McpToolParameterType.STRING_LIST, description = "Ids of tags to link."),
                        McpToolParameter(name = "placeIds", type = McpToolParameterType.STRING_LIST, description = "Ids of places to link."),
                        McpToolParameter(name = "webIds", type = McpToolParameterType.STRING_LIST, description = "Ids of webs to link."),
                        McpToolParameter(name = "contactIds", type = McpToolParameterType.STRING_LIST, description = "Ids of contacts to link."),
                    ),
                kind = McpToolKind.WRITE,
            ) { arguments ->
                appActionCall {
                    memoAppAction.add(
                        title = arguments.string(TITLE),
                        description = arguments.stringOrNull(DESCRIPTION),
                        color = arguments.colorTextOrNull(),
                        dateTime = arguments.memoDateTimeInputOrNull(),
                        primaryTagId = arguments.idTextOrNull("primaryTagId"),
                        tagIds = arguments.idTextList("tagIds"),
                        placeIds = arguments.idTextList("placeIds"),
                        webIds = arguments.idTextList("webIds"),
                        contactIds = arguments.idTextList("contactIds"),
                    )
                }.toJson()
            },
            McpTool(
                name = "memo_update",
                description =
                    "Updates the title, description, color or period of a memo. Omitted fields keep their current value. " +
                        "A blank title keeps the current title. Links are changed with the memo link tools.",
                parameterList =
                    listOf(
                        memoIdParameter,
                        McpToolParameter(name = TITLE, type = McpToolParameterType.STRING, description = "New title."),
                        McpToolParameter(name = DESCRIPTION, type = McpToolParameterType.STRING, description = "New description."),
                        colorParameter(description = "New color."),
                        memoDateTimeParameter,
                        McpToolParameter(name = "clearDateTime", type = McpToolParameterType.BOOLEAN, description = "Set true to remove the period."),
                    ),
                kind = McpToolKind.WRITE,
            ) { arguments -> updateMemo(arguments) },
            McpTool(
                name = "memo_copy",
                description = "Copies a memo with its links into a new unfinished memo.",
                parameterList = listOf(memoIdParameter),
                kind = McpToolKind.WRITE,
            ) { arguments -> appActionCall { memoAppAction.copy(memoId = arguments.idText(MEMO_ID)) }.toJson() },
            McpTool(
                name = "memo_finish",
                description = "Marks a memo as finished.",
                parameterList = listOf(memoIdParameter),
                kind = McpToolKind.WRITE,
            ) { arguments -> appActionCall { memoAppAction.finish(memoId = arguments.idText(MEMO_ID)) }.toJson() },
            McpTool(
                name = "memo_restart",
                description = "Marks a finished memo as not finished.",
                parameterList = listOf(memoIdParameter),
                kind = McpToolKind.WRITE,
            ) { arguments -> appActionCall { memoAppAction.restart(memoId = arguments.idText(MEMO_ID)) }.toJson() },
            McpTool(
                name = "memo_delete",
                description = "Deletes a memo. Use memo_restore with the same id to undo.",
                parameterList = listOf(memoIdParameter),
                kind = McpToolKind.DESTRUCTIVE,
            ) { arguments -> appActionCall { memoAppAction.delete(memoId = arguments.idText(MEMO_ID)) }.toJson() },
            McpTool(
                name = "memo_restore",
                description = "Restores a deleted memo.",
                parameterList = listOf(memoIdParameter),
                kind = McpToolKind.WRITE,
            ) { arguments -> appActionCall { memoAppAction.restore(memoId = arguments.idText(MEMO_ID)) }.toJson() },
        )

    private suspend fun updateMemo(arguments: McpToolArguments) =
        run {
            val memoId = arguments.uuid(MEMO_ID)
            val current = findMemoUseCase.readActive(id = memoId, isDeleted = Memo::isDeleted)?.detail ?: return@run changedCountResult(count = 0)
            val detail =
                MemoDetail(
                    title = arguments.stringOrNull(TITLE) ?: current.title,
                    description = arguments.stringOrNull(DESCRIPTION) ?: current.description,
                    color = arguments.colorOrNull() ?: current.color,
                    dateTime =
                        when {
                            arguments.booleanOrNull("clearDateTime") == true -> null
                            arguments.has("dateTime") -> arguments.memoDateTimeOrNull()
                            else -> current.dateTime
                        },
                )

            changedCountResult(updateMemoUseCase(parameter = UpdateMemoUseCase.Parameter(id = memoId, detail = detail)).getOrThrow())
        }
}
