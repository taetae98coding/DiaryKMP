package io.github.taetae98coding.diary.work.mcp.tool.tag

import io.github.taetae98coding.diary.work.appaction.tag.AppActionEntityTagKind
import io.github.taetae98coding.diary.work.appaction.tag.EntityTagAppAction
import io.github.taetae98coding.diary.work.mcp.tool.McpTool
import io.github.taetae98coding.diary.work.mcp.tool.McpToolKind
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameter
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameterType
import io.github.taetae98coding.diary.work.mcp.tool.appActionCall
import io.github.taetae98coding.diary.work.mcp.tool.idText
import io.github.taetae98coding.diary.work.mcp.tool.json.toJson

private const val TAG_ID = "tagId"

internal const val NOT_DONE_DESCRIPTION: String = "Returns done false and changes nothing when either item does not exist or is deleted."

internal val tagIdsParameter: McpToolParameter =
    McpToolParameter(name = "tagIds", type = McpToolParameterType.STRING_LIST, description = "Ids of tags to link.")

internal fun entityTagToolList(
    toolNamePrefix: String,
    entityName: String,
    kind: AppActionEntityTagKind,
    entityIdParameter: McpToolParameter,
    entityTagAppAction: EntityTagAppAction,
): List<McpTool> {
    val parameterList =
        listOf(
            entityIdParameter,
            McpToolParameter(name = TAG_ID, type = McpToolParameterType.STRING, description = "Id of the tag.", isRequired = true),
        )

    return listOf(
        McpTool(
            name = "${toolNamePrefix}_tag_add",
            description = "Links a tag to $entityName. $NOT_DONE_DESCRIPTION",
            parameterList = parameterList,
            kind = McpToolKind.WRITE,
        ) { arguments ->
            appActionCall { entityTagAppAction.add(kind = kind, entityId = arguments.idText(entityIdParameter.name), tagId = arguments.idText(TAG_ID)) }.toJson()
        },
        McpTool(
            name = "${toolNamePrefix}_tag_remove",
            description = "Unlinks a tag from $entityName. $NOT_DONE_DESCRIPTION",
            parameterList = parameterList,
            kind = McpToolKind.WRITE,
        ) { arguments ->
            appActionCall { entityTagAppAction.remove(kind = kind, entityId = arguments.idText(entityIdParameter.name), tagId = arguments.idText(TAG_ID)) }.toJson()
        },
    )
}
