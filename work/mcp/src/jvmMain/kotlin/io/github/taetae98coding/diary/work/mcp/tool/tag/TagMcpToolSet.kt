package io.github.taetae98coding.diary.work.mcp.tool.tag

import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.domain.tag.usecase.FindTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.UpdateTagUseCase
import io.github.taetae98coding.diary.work.appaction.core.readActive
import io.github.taetae98coding.diary.work.appaction.tag.AppActionTagDetail
import io.github.taetae98coding.diary.work.appaction.tag.TagAppAction
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
import io.github.taetae98coding.diary.work.mcp.tool.json.changedCountResult
import io.github.taetae98coding.diary.work.mcp.tool.json.colorOrNull
import io.github.taetae98coding.diary.work.mcp.tool.json.colorParameter
import io.github.taetae98coding.diary.work.mcp.tool.json.sortParameter
import io.github.taetae98coding.diary.work.mcp.tool.json.toJson
import io.github.taetae98coding.diary.work.mcp.tool.json.toJsonArray
import io.github.taetae98coding.diary.work.mcp.tool.paging.mcpPageCursorParameter
import io.github.taetae98coding.diary.work.mcp.tool.paging.toJson
import io.github.taetae98coding.diary.work.mcp.tool.sortText
import io.github.taetae98coding.diary.work.mcp.tool.uuid
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
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

@Factory(binds = [McpToolSet::class])
internal class TagMcpToolSet(
    private val tagAppAction: TagAppAction,
    private val findTagUseCase: FindTagUseCase,
    private val updateTagUseCase: UpdateTagUseCase,
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
                appActionCall { tagAppAction.list(sort = arguments.sortText(tagSortList), cursor = arguments.cursorText()) }.toJson { tag -> tag.toJson() }
            },
            McpTool(
                name = "tag_finished_list",
                description = "Lists finished tags that are not deleted.",
                parameterList = listOf(sortParameter(tagSortList), mcpPageCursorParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                appActionCall { tagAppAction.finishedList(sort = arguments.sortText(tagSortList), cursor = arguments.cursorText()) }.toJson { tag -> tag.toJson() }
            },
            McpTool(
                name = "tag_get",
                description =
                    "Gets one tag with the tags it links to (its child tags). Returns null when the tag does not exist or is deleted. " +
                        "Use the tag item list tools such as tag_memo_list and tag_web_list for the items linked to the tag.",
                parameterList = listOf(tagIdParameter),
                kind = McpToolKind.READ,
            ) { arguments -> appActionCall { tagAppAction.get(tagId = arguments.idText(TAG_ID)) }?.toJson() ?: JsonNull },
            McpTool(
                name = "tag_add",
                description = "Adds a tag. The title must not be blank.",
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
                appActionCall {
                    tagAppAction.add(
                        emoji = arguments.stringOrNull(EMOJI),
                        title = arguments.string(TITLE),
                        description = arguments.stringOrNull(DESCRIPTION),
                        color = arguments.colorTextOrNull(),
                        linkedTagIds = arguments.idTextList("linkedTagIds"),
                    )
                }.toJson()
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
            ) { arguments -> appActionCall { tagAppAction.finish(tagId = arguments.idText(TAG_ID)) }.toJson() },
            McpTool(
                name = "tag_restart",
                description = "Marks a finished tag as not finished.",
                parameterList = listOf(tagIdParameter),
                kind = McpToolKind.WRITE,
            ) { arguments -> appActionCall { tagAppAction.restart(tagId = arguments.idText(TAG_ID)) }.toJson() },
            McpTool(
                name = "tag_delete",
                description = "Deletes a tag. Use tag_restore with the same id to undo.",
                parameterList = listOf(tagIdParameter),
                kind = McpToolKind.DESTRUCTIVE,
            ) { arguments -> appActionCall { tagAppAction.delete(tagId = arguments.idText(TAG_ID)) }.toJson() },
            McpTool(
                name = "tag_restore",
                description = "Restores a deleted tag.",
                parameterList = listOf(tagIdParameter),
                kind = McpToolKind.WRITE,
            ) { arguments -> appActionCall { tagAppAction.restore(tagId = arguments.idText(TAG_ID)) }.toJson() },
        )

    private suspend fun updateTag(arguments: McpToolArguments) =
        run {
            val tagId = arguments.uuid(TAG_ID)
            val current = findTagUseCase.readActive(id = tagId, isDeleted = Tag::isDeleted)?.detail ?: return@run changedCountResult(count = 0)
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

private fun AppActionTagDetail.toJson(): JsonObject =
    buildJsonObject {
        put("tag", tag.toJson())
        put("linkedTags", linkedTags.toJsonArray { linkedTag -> linkedTag.toJson() })
    }
