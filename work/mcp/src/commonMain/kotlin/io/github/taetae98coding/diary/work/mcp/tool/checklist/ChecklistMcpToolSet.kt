package io.github.taetae98coding.diary.work.mcp.tool.checklist

import io.github.taetae98coding.diary.core.model.checklist.ChecklistDetail
import io.github.taetae98coding.diary.domain.checklist.usecase.AddChecklistItemUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.AddChecklistUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.CheckChecklistItemUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.DeleteChecklistItemUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.DeleteChecklistUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.FindChecklistUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.GetChecklistItemUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.PageChecklistUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.ResetChecklistUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.RestoreChecklistUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.UncheckChecklistItemUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.UpdateChecklistUseCase
import io.github.taetae98coding.diary.work.mcp.tool.McpTool
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArguments
import io.github.taetae98coding.diary.work.mcp.tool.McpToolKind
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameter
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameterType
import io.github.taetae98coding.diary.work.mcp.tool.McpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.json.changedCountResult
import io.github.taetae98coding.diary.work.mcp.tool.json.idResult
import io.github.taetae98coding.diary.work.mcp.tool.json.toJsonArray
import io.github.taetae98coding.diary.work.mcp.tool.paging.mcpPageCursorParameter
import io.github.taetae98coding.diary.work.mcp.tool.paging.pageOffset
import io.github.taetae98coding.diary.work.mcp.tool.paging.readPage
import io.github.taetae98coding.diary.work.mcp.tool.paging.toJson
import io.github.taetae98coding.diary.work.mcp.tool.readFirst
import io.github.taetae98coding.diary.work.mcp.tool.uuid
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.koin.core.annotation.Factory

private const val CHECKLIST_ID = "checklistId"
private const val DESCRIPTION = "description"
private const val ITEM_ID = "itemId"
private const val ITEMS = "items"
private const val TITLE = "title"

private val checklistIdParameter: McpToolParameter =
    McpToolParameter(name = CHECKLIST_ID, type = McpToolParameterType.STRING, description = "Id of the checklist.", isRequired = true)

private val checklistItemIdParameter: McpToolParameter =
    McpToolParameter(name = ITEM_ID, type = McpToolParameterType.STRING, description = "Id of the checklist item.", isRequired = true)

@Factory
internal class ChecklistMcpToolSet(
    private val pageChecklistUseCase: PageChecklistUseCase,
    private val findChecklistUseCase: FindChecklistUseCase,
    private val getChecklistItemUseCase: GetChecklistItemUseCase,
    private val addChecklistUseCase: AddChecklistUseCase,
    private val updateChecklistUseCase: UpdateChecklistUseCase,
    private val resetChecklistUseCase: ResetChecklistUseCase,
    private val deleteChecklistUseCase: DeleteChecklistUseCase,
    private val restoreChecklistUseCase: RestoreChecklistUseCase,
    private val addChecklistItemUseCase: AddChecklistItemUseCase,
    private val checkChecklistItemUseCase: CheckChecklistItemUseCase,
    private val uncheckChecklistItemUseCase: UncheckChecklistItemUseCase,
    private val deleteChecklistItemUseCase: DeleteChecklistItemUseCase,
) : McpToolSet {
    override val toolList: List<McpTool> =
        listOf(
            McpTool(
                name = "checklist_list",
                description = "Lists checklists that are not deleted, by title, with how many of their items are checked.",
                parameterList = listOf(mcpPageCursorParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                pageChecklistUseCase
                    .readPage(parameter = Unit, offset = arguments.pageOffset())
                    .toJson { summary -> summary.toJson() }
            },
            McpTool(
                name = "checklist_get",
                description = "Gets one checklist with all its items that are not deleted. Returns null when the checklist does not exist.",
                parameterList = listOf(checklistIdParameter, checklistItemSortParameter),
                kind = McpToolKind.READ,
            ) { arguments -> getChecklist(arguments) },
            McpTool(
                name = "checklist_add",
                description = "Adds a checklist with optional unchecked items. The title must not be blank. Blank item contents are skipped.",
                parameterList =
                    listOf(
                        McpToolParameter(name = TITLE, type = McpToolParameterType.STRING, description = "Title of the checklist.", isRequired = true),
                        McpToolParameter(name = DESCRIPTION, type = McpToolParameterType.STRING, description = "Description of the checklist."),
                        McpToolParameter(name = ITEMS, type = McpToolParameterType.STRING_LIST, description = "Contents of the items, in the order to add them."),
                    ),
                kind = McpToolKind.WRITE,
            ) { arguments ->
                val id =
                    addChecklistUseCase(
                        parameter =
                            AddChecklistUseCase.Parameter(
                                detail =
                                    ChecklistDetail(
                                        title = arguments.string(TITLE),
                                        description = arguments.stringOrNull(DESCRIPTION).orEmpty(),
                                    ),
                                itemContentList = arguments.stringList(ITEMS),
                                tagIdSet = emptySet(),
                            ),
                    ).getOrThrow()

                idResult(id)
            },
            McpTool(
                name = "checklist_update",
                description =
                    "Updates the title or description of a checklist. Omitted fields keep their current value. A blank title keeps the current title. " +
                        "Items are changed with the checklist item tools.",
                parameterList =
                    listOf(
                        checklistIdParameter,
                        McpToolParameter(name = TITLE, type = McpToolParameterType.STRING, description = "New title."),
                        McpToolParameter(name = DESCRIPTION, type = McpToolParameterType.STRING, description = "New description."),
                    ),
                kind = McpToolKind.WRITE,
            ) { arguments -> updateChecklist(arguments) },
            McpTool(
                name = "checklist_reset",
                description = "Unchecks all checked items of a checklist at once. Returns how many items were unchecked.",
                parameterList = listOf(checklistIdParameter),
                kind = McpToolKind.WRITE,
            ) { arguments -> changedCountResult(resetChecklistUseCase(parameter = arguments.uuid(CHECKLIST_ID)).getOrThrow()) },
            McpTool(
                name = "checklist_delete",
                description = "Deletes a checklist. Use checklist_restore with the same id to undo.",
                parameterList = listOf(checklistIdParameter),
                kind = McpToolKind.DESTRUCTIVE,
            ) { arguments -> changedCountResult(deleteChecklistUseCase(parameter = arguments.uuid(CHECKLIST_ID)).getOrThrow()) },
            McpTool(
                name = "checklist_restore",
                description = "Restores a deleted checklist.",
                parameterList = listOf(checklistIdParameter),
                kind = McpToolKind.WRITE,
            ) { arguments -> changedCountResult(restoreChecklistUseCase(parameter = arguments.uuid(CHECKLIST_ID)).getOrThrow()) },
            McpTool(
                name = "checklist_item_add",
                description = "Adds an unchecked item to the end of a checklist. The content must not be blank. The same content can be added more than once.",
                parameterList =
                    listOf(
                        checklistIdParameter,
                        McpToolParameter(name = "content", type = McpToolParameterType.STRING, description = "Content of the item, one line.", isRequired = true),
                    ),
                kind = McpToolKind.WRITE,
            ) { arguments ->
                val addedCount =
                    addChecklistItemUseCase(
                        parameter = AddChecklistItemUseCase.Parameter(checklistId = arguments.uuid(CHECKLIST_ID), content = arguments.string("content")),
                    ).getOrThrow()

                changedCountResult(addedCount)
            },
            McpTool(
                name = "checklist_item_check",
                description = "Checks a checklist item.",
                parameterList = listOf(checklistItemIdParameter),
                kind = McpToolKind.WRITE,
            ) { arguments -> changedCountResult(checkChecklistItemUseCase(parameter = arguments.uuid(ITEM_ID)).getOrThrow()) },
            McpTool(
                name = "checklist_item_uncheck",
                description = "Unchecks a checklist item.",
                parameterList = listOf(checklistItemIdParameter),
                kind = McpToolKind.WRITE,
            ) { arguments -> changedCountResult(uncheckChecklistItemUseCase(parameter = arguments.uuid(ITEM_ID)).getOrThrow()) },
            McpTool(
                name = "checklist_item_delete",
                description = "Removes an item from its checklist. This cannot be undone; add the content again with checklist_item_add if needed.",
                parameterList = listOf(checklistItemIdParameter),
                kind = McpToolKind.DESTRUCTIVE,
            ) { arguments -> changedCountResult(deleteChecklistItemUseCase(parameter = arguments.uuid(ITEM_ID)).getOrThrow()) },
        )

    private suspend fun getChecklist(arguments: McpToolArguments): JsonElement {
        val checklistId = arguments.uuid(CHECKLIST_ID)
        val checklist = findChecklistUseCase.readFirst(parameter = checklistId) ?: return JsonNull
        val itemList =
            getChecklistItemUseCase.readFirst(
                parameter = GetChecklistItemUseCase.Parameter(checklistId = checklistId, sort = arguments.checklistItemSort()),
            )

        return buildJsonObject {
            put("checklist", checklist.toJson())
            put(ITEMS, itemList.toJsonArray { item -> item.toJson() })
        }
    }

    private suspend fun updateChecklist(arguments: McpToolArguments): JsonElement {
        val checklistId = arguments.uuid(CHECKLIST_ID)
        val current = findChecklistUseCase.readFirst(parameter = checklistId)?.detail ?: return changedCountResult(count = 0)
        val detail =
            ChecklistDetail(
                title = arguments.stringOrNull(TITLE) ?: current.title,
                description = arguments.stringOrNull(DESCRIPTION) ?: current.description,
            )

        return changedCountResult(updateChecklistUseCase(parameter = UpdateChecklistUseCase.Parameter(id = checklistId, detail = detail)).getOrThrow())
    }
}
