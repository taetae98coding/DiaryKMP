package io.github.taetae98coding.diary.work.mcp.tool.tag

import io.github.taetae98coding.diary.work.appaction.tag.TagLinkAppAction
import io.github.taetae98coding.diary.work.mcp.tool.McpTool
import io.github.taetae98coding.diary.work.mcp.tool.McpToolKind
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameter
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameterType
import io.github.taetae98coding.diary.work.mcp.tool.McpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.appActionCall
import io.github.taetae98coding.diary.work.mcp.tool.idText
import io.github.taetae98coding.diary.work.mcp.tool.json.toJson
import org.koin.core.annotation.Factory

private const val LINKED_TAG_ID = "linkedTagId"

private val linkedTagIdParameter: McpToolParameter =
    McpToolParameter(name = LINKED_TAG_ID, type = McpToolParameterType.STRING, description = "Id of the tag that the tag links to (the child tag).", isRequired = true)

@Factory(binds = [McpToolSet::class])
internal class TagLinkMcpToolSet(
    private val tagLinkAppAction: TagLinkAppAction,
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
                appActionCall { tagLinkAppAction.add(tagId = arguments.idText("tagId"), linkedTagId = arguments.idText(LINKED_TAG_ID)) }.toJson()
            },
            McpTool(
                name = "tag_link_remove",
                description = "Removes the link from a tag to another tag. The link in the opposite direction is kept. $NOT_DONE_DESCRIPTION",
                parameterList = listOf(tagIdParameter, linkedTagIdParameter),
                kind = McpToolKind.WRITE,
            ) { arguments ->
                appActionCall { tagLinkAppAction.remove(tagId = arguments.idText("tagId"), linkedTagId = arguments.idText(LINKED_TAG_ID)) }.toJson()
            },
        )
}
