package io.github.taetae98coding.diary.work.mcp.tool.tag

import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.domain.tag.usecase.AddTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.DeleteTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.FindTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.FinishTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.GetLinkedTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.PageFinishedTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.PageTagHomeUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.RestartTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.RestoreTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.UpdateTagUseCase
import io.github.taetae98coding.diary.work.mcp.tool.McpTool
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArguments
import io.github.taetae98coding.diary.work.mcp.tool.McpToolKind
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameter
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameterType
import io.github.taetae98coding.diary.work.mcp.tool.McpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.json.changedCountResult
import io.github.taetae98coding.diary.work.mcp.tool.json.colorOrNull
import io.github.taetae98coding.diary.work.mcp.tool.json.colorOrRandom
import io.github.taetae98coding.diary.work.mcp.tool.json.colorParameter
import io.github.taetae98coding.diary.work.mcp.tool.json.idResult
import io.github.taetae98coding.diary.work.mcp.tool.json.sort
import io.github.taetae98coding.diary.work.mcp.tool.json.sortParameter
import io.github.taetae98coding.diary.work.mcp.tool.json.toJson
import io.github.taetae98coding.diary.work.mcp.tool.json.toJsonArray
import io.github.taetae98coding.diary.work.mcp.tool.paging.mcpPageCursorParameter
import io.github.taetae98coding.diary.work.mcp.tool.paging.pageOffset
import io.github.taetae98coding.diary.work.mcp.tool.paging.readPage
import io.github.taetae98coding.diary.work.mcp.tool.paging.toJson
import io.github.taetae98coding.diary.work.mcp.tool.readFirst
import io.github.taetae98coding.diary.work.mcp.tool.uuid
import io.github.taetae98coding.diary.work.mcp.tool.uuidSet
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.koin.core.annotation.Factory

private const val DESCRIPTION = "description"
private const val EMOJI = "emoji"
private const val TAG_ID = "tagId"
private const val TITLE = "title"

internal val tagSortList: List<ListSort> = listOf(ListSort.TITLE, ListSort.RECENTLY_UPDATED)

internal val tagIdParameter: McpToolParameter =
    McpToolParameter(name = TAG_ID, type = McpToolParameterType.STRING, description = "Id of the tag.", isRequired = true)

@Factory
internal class TagMcpToolSet(
    private val pageTagHomeUseCase: PageTagHomeUseCase,
    private val pageFinishedTagUseCase: PageFinishedTagUseCase,
    private val findTagUseCase: FindTagUseCase,
    private val getLinkedTagUseCase: GetLinkedTagUseCase,
    private val addTagUseCase: AddTagUseCase,
    private val updateTagUseCase: UpdateTagUseCase,
    private val finishTagUseCase: FinishTagUseCase,
    private val restartTagUseCase: RestartTagUseCase,
    private val deleteTagUseCase: DeleteTagUseCase,
    private val restoreTagUseCase: RestoreTagUseCase,
) : McpToolSet {
    override val toolList: List<McpTool> =
        listOf(
            McpTool(
                name = "tag_list",
                description =
                    "Lists the tags shown on the tag home list: tags that are neither finished nor deleted. " +
                        "When the user turned on the top-level-only filter in the app, only tags that no other tag links to are listed.",
                parameterList = listOf(sortParameter(tagSortList), mcpPageCursorParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                pageTagHomeUseCase
                    .readPage(parameter = arguments.sort(tagSortList), offset = arguments.pageOffset())
                    .toJson { tag -> tag.toJson() }
            },
            McpTool(
                name = "tag_finished_list",
                description = "Lists finished tags that are not deleted.",
                parameterList = listOf(sortParameter(tagSortList), mcpPageCursorParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                pageFinishedTagUseCase
                    .readPage(parameter = arguments.sort(tagSortList), offset = arguments.pageOffset())
                    .toJson { tag -> tag.toJson() }
            },
            McpTool(
                name = "tag_get",
                description =
                    "Gets one tag with the tags it links to (its child tags). Returns null when the tag does not exist. " +
                        "Use tag_memo_list, tag_web_list and tag_place_list for the items linked to the tag.",
                parameterList = listOf(tagIdParameter),
                kind = McpToolKind.READ,
            ) { arguments -> getTag(arguments) },
            McpTool(
                name = "tag_add",
                description = "Adds a tag. The title must not be blank. Linked tags must exist and not be deleted.",
                parameterList =
                    listOf(
                        McpToolParameter(name = TITLE, type = McpToolParameterType.STRING, description = "Title of the tag.", isRequired = true),
                        McpToolParameter(name = EMOJI, type = McpToolParameterType.STRING, description = "A single emoji shown before the title."),
                        McpToolParameter(name = DESCRIPTION, type = McpToolParameterType.STRING, description = "Description of the tag."),
                        colorParameter(description = "Color of the tag. A random color is used when omitted."),
                        McpToolParameter(name = "linkedTagIds", type = McpToolParameterType.STRING_LIST, description = "Ids of tags the new tag links to as its child tags."),
                    ),
                kind = McpToolKind.WRITE,
            ) { arguments ->
                val id =
                    addTagUseCase(
                        parameter =
                            AddTagUseCase.Parameter(
                                detail =
                                    TagDetail(
                                        emoji = arguments.stringOrNull(EMOJI).orEmpty(),
                                        title = arguments.string(TITLE),
                                        description = arguments.stringOrNull(DESCRIPTION).orEmpty(),
                                        color = arguments.colorOrRandom(),
                                    ),
                                linkedTagIdSet = arguments.uuidSet("linkedTagIds"),
                            ),
                    ).getOrThrow()

                idResult(id)
            },
            McpTool(
                name = "tag_update",
                description =
                    "Updates the emoji, title, description or color of a tag. Omitted fields keep their current value. " +
                        "A blank title keeps the current title. Set emoji to an empty string to remove it. Links are changed with tag_link_add and tag_link_remove.",
                parameterList =
                    listOf(
                        tagIdParameter,
                        McpToolParameter(name = EMOJI, type = McpToolParameterType.STRING, description = "New emoji."),
                        McpToolParameter(name = TITLE, type = McpToolParameterType.STRING, description = "New title."),
                        McpToolParameter(name = DESCRIPTION, type = McpToolParameterType.STRING, description = "New description."),
                        colorParameter(description = "New color."),
                    ),
                kind = McpToolKind.WRITE,
            ) { arguments -> updateTag(arguments) },
            McpTool(
                name = "tag_finish",
                description = "Marks a tag as finished.",
                parameterList = listOf(tagIdParameter),
                kind = McpToolKind.WRITE,
            ) { arguments -> changedCountResult(finishTagUseCase(parameter = arguments.uuid(TAG_ID)).getOrThrow()) },
            McpTool(
                name = "tag_restart",
                description = "Marks a finished tag as not finished.",
                parameterList = listOf(tagIdParameter),
                kind = McpToolKind.WRITE,
            ) { arguments -> changedCountResult(restartTagUseCase(parameter = arguments.uuid(TAG_ID)).getOrThrow()) },
            McpTool(
                name = "tag_delete",
                description = "Deletes a tag. Use tag_restore with the same id to undo.",
                parameterList = listOf(tagIdParameter),
                kind = McpToolKind.DESTRUCTIVE,
            ) { arguments -> changedCountResult(deleteTagUseCase(parameter = arguments.uuid(TAG_ID)).getOrThrow()) },
            McpTool(
                name = "tag_restore",
                description = "Restores a deleted tag.",
                parameterList = listOf(tagIdParameter),
                kind = McpToolKind.WRITE,
            ) { arguments -> changedCountResult(restoreTagUseCase(parameter = arguments.uuid(TAG_ID)).getOrThrow()) },
        )

    private suspend fun getTag(arguments: McpToolArguments) =
        run {
            val tagId = arguments.uuid(TAG_ID)
            val tag = findTagUseCase.readFirst(parameter = tagId) ?: return@run JsonNull

            buildJsonObject {
                put("tag", tag.toJson())
                put("linkedTags", getLinkedTagUseCase.readFirst(parameter = tagId).toJsonArray { linkedTag -> linkedTag.toJson() })
            }
        }

    private suspend fun updateTag(arguments: McpToolArguments) =
        run {
            val tagId = arguments.uuid(TAG_ID)
            val current = findTagUseCase.readFirst(parameter = tagId)?.detail ?: return@run changedCountResult(count = 0)
            val detail =
                TagDetail(
                    emoji = arguments.stringOrNull(EMOJI) ?: current.emoji,
                    title = arguments.stringOrNull(TITLE) ?: current.title,
                    description = arguments.stringOrNull(DESCRIPTION) ?: current.description,
                    color = arguments.colorOrNull() ?: current.color,
                )

            changedCountResult(updateTagUseCase(parameter = UpdateTagUseCase.Parameter(id = tagId, detail = detail)).getOrThrow())
        }
}
