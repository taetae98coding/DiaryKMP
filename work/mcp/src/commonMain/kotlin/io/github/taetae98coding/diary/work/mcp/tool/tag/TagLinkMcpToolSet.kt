package io.github.taetae98coding.diary.work.mcp.tool.tag

import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.tag.usecase.AddTagLinkUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.FindTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.RemoveTagLinkUseCase
import io.github.taetae98coding.diary.work.mcp.tool.McpTool
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArguments
import io.github.taetae98coding.diary.work.mcp.tool.McpToolKind
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameter
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameterType
import io.github.taetae98coding.diary.work.mcp.tool.McpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.isActive
import io.github.taetae98coding.diary.work.mcp.tool.json.doneResult
import io.github.taetae98coding.diary.work.mcp.tool.json.notDoneResult
import io.github.taetae98coding.diary.work.mcp.tool.uuid
import kotlinx.serialization.json.JsonElement
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

private const val LINKED_TAG_ID = "linkedTagId"

private val linkedTagIdParameter: McpToolParameter =
    McpToolParameter(name = LINKED_TAG_ID, type = McpToolParameterType.STRING, description = "Id of the tag that the tag links to (the child tag).", isRequired = true)

@Factory
internal class TagLinkMcpToolSet(
    private val findTagUseCase: FindTagUseCase,
    private val addTagLinkUseCase: AddTagLinkUseCase,
    private val removeTagLinkUseCase: RemoveTagLinkUseCase,
) : McpToolSet {
    override val toolList: List<McpTool> =
        listOf(
            McpTool(
                name = "tag_link_add",
                description =
                    "Links a tag to another tag, making the linked tag a child of the tag. Links are one-way and a tag cannot link to itself. " +
                        "Linking does not change the tags of memos, webs or places. $NOT_DONE_DESCRIPTION",
                parameterList = listOf(tagIdParameter, linkedTagIdParameter),
                kind = McpToolKind.WRITE,
            ) { arguments ->
                link(arguments) { tagId, linkedTagId -> addTagLinkUseCase(parameter = AddTagLinkUseCase.Parameter(fromTagId = tagId, toTagId = linkedTagId)).getOrThrow() }
            },
            McpTool(
                name = "tag_link_remove",
                description = "Removes the link from a tag to another tag. The link in the opposite direction is kept. $NOT_DONE_DESCRIPTION",
                parameterList = listOf(tagIdParameter, linkedTagIdParameter),
                kind = McpToolKind.WRITE,
            ) { arguments ->
                link(arguments) { tagId, linkedTagId -> removeTagLinkUseCase(parameter = RemoveTagLinkUseCase.Parameter(fromTagId = tagId, toTagId = linkedTagId)).getOrThrow() }
            },
        )

    private suspend fun link(
        arguments: McpToolArguments,
        block: suspend (tagId: Uuid, linkedTagId: Uuid) -> Unit,
    ): JsonElement {
        val tagId = arguments.uuid("tagId")
        val linkedTagId = arguments.uuid(LINKED_TAG_ID)
        val isLinkable = findTagUseCase.isActive(id = tagId, isDeleted = Tag::isDeleted) && findTagUseCase.isActive(id = linkedTagId, isDeleted = Tag::isDeleted)

        if (!isLinkable) return notDoneResult

        block(tagId, linkedTagId)

        return doneResult
    }
}
