package io.github.taetae98coding.diary.work.mcp.tool.tag

import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.tag.EntityTagKind
import io.github.taetae98coding.diary.domain.tag.usecase.AddEntityTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.FindTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.RemoveEntityTagUseCase
import io.github.taetae98coding.diary.work.mcp.tool.McpTool
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArguments
import io.github.taetae98coding.diary.work.mcp.tool.McpToolKind
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameter
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameterType
import io.github.taetae98coding.diary.work.mcp.tool.isActive
import io.github.taetae98coding.diary.work.mcp.tool.json.doneResult
import io.github.taetae98coding.diary.work.mcp.tool.json.notDoneResult
import io.github.taetae98coding.diary.work.mcp.tool.uuid
import kotlinx.serialization.json.JsonElement
import kotlin.uuid.Uuid

private const val TAG_ID = "tagId"

internal const val NOT_DONE_DESCRIPTION: String = "Returns done false and changes nothing when either item does not exist or is deleted."

internal val tagIdsParameter: McpToolParameter =
    McpToolParameter(name = "tagIds", type = McpToolParameterType.STRING_LIST, description = "Ids of tags to link.")

internal fun entityTagToolList(
    toolNamePrefix: String,
    entityName: String,
    kind: EntityTagKind,
    entityIdParameter: McpToolParameter,
    findTagUseCase: FindTagUseCase,
    addEntityTagUseCase: AddEntityTagUseCase,
    removeEntityTagUseCase: RemoveEntityTagUseCase,
    isEntityActive: suspend (Uuid) -> Boolean,
): List<McpTool> {
    val parameterList =
        listOf(
            entityIdParameter,
            McpToolParameter(name = TAG_ID, type = McpToolParameterType.STRING, description = "Id of the tag.", isRequired = true),
        )

    suspend fun link(
        arguments: McpToolArguments,
        block: suspend (entityId: Uuid, tagId: Uuid) -> Unit,
    ): JsonElement {
        val entityId = arguments.uuid(entityIdParameter.name)
        val tagId = arguments.uuid(TAG_ID)

        if (!isEntityActive(entityId) || !findTagUseCase.isActive(id = tagId, isDeleted = Tag::isDeleted)) return notDoneResult

        block(entityId, tagId)

        return doneResult
    }

    return listOf(
        McpTool(
            name = "${toolNamePrefix}_tag_add",
            description = "Links a tag to $entityName. $NOT_DONE_DESCRIPTION",
            parameterList = parameterList,
            kind = McpToolKind.WRITE,
        ) { arguments ->
            link(arguments) { entityId, tagId -> addEntityTagUseCase(parameter = AddEntityTagUseCase.Parameter(kind = kind, entityId = entityId, tagId = tagId)).getOrThrow() }
        },
        McpTool(
            name = "${toolNamePrefix}_tag_remove",
            description = "Unlinks a tag from $entityName. $NOT_DONE_DESCRIPTION",
            parameterList = parameterList,
            kind = McpToolKind.WRITE,
        ) { arguments ->
            link(arguments) { entityId, tagId -> removeEntityTagUseCase(parameter = RemoveEntityTagUseCase.Parameter(kind = kind, entityId = entityId, tagId = tagId)).getOrThrow() }
        },
    )
}
