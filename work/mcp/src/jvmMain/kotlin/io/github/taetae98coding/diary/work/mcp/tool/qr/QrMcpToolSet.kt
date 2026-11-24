package io.github.taetae98coding.diary.work.mcp.tool.qr

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import io.github.taetae98coding.diary.core.model.qr.Qr
import io.github.taetae98coding.diary.core.model.qr.QrDetail
import io.github.taetae98coding.diary.domain.qr.usecase.FindQrUseCase
import io.github.taetae98coding.diary.domain.qr.usecase.UpdateQrUseCase
import io.github.taetae98coding.diary.work.appaction.core.readActive
import io.github.taetae98coding.diary.work.appaction.qr.AppActionQr
import io.github.taetae98coding.diary.work.appaction.qr.AppActionQrDetail
import io.github.taetae98coding.diary.work.appaction.qr.QrAppAction
import io.github.taetae98coding.diary.work.appaction.tag.AppActionEntityTagKind
import io.github.taetae98coding.diary.work.appaction.tag.EntityTagAppAction
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
import io.github.taetae98coding.diary.work.mcp.tool.json.toJson
import io.github.taetae98coding.diary.work.mcp.tool.json.toJsonArray
import io.github.taetae98coding.diary.work.mcp.tool.paging.mcpPageCursorParameter
import io.github.taetae98coding.diary.work.mcp.tool.paging.toJson
import io.github.taetae98coding.diary.work.mcp.tool.tag.entityTagToolList
import io.github.taetae98coding.diary.work.mcp.tool.tag.tagIdsParameter
import io.github.taetae98coding.diary.work.mcp.tool.uuid
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

private const val DESCRIPTION = "description"
private const val TITLE = "title"
private const val VALUE = "value"

private val qrIdParameter: McpToolParameter =
    McpToolParameter(name = "qrId", type = McpToolParameterType.STRING, description = "Id of the QR.", isRequired = true)

@Inject
@ContributesIntoSet(AppScope::class, binding = binding<McpToolSet>())
internal class QrMcpToolSet(
    private val qrAppAction: QrAppAction,
    private val entityTagAppAction: EntityTagAppAction,
    private val findQrUseCase: FindQrUseCase,
    private val updateQrUseCase: UpdateQrUseCase,
) : McpToolSet {
    override val toolList: List<McpTool> =
        listOf(
            McpTool(
                name = "qr_list",
                description = "Lists saved QR codes that are not deleted, ordered by title.",
                parameterList = listOf(mcpPageCursorParameter),
                kind = McpToolKind.READ,
            ) { arguments -> appActionCall { qrAppAction.list(cursor = arguments.cursorText()) }.toJson { qr -> qr.toJson() } },
            McpTool(
                name = "qr_get",
                description = "Gets one saved QR code with its linked tags. Returns null when the QR does not exist or is deleted.",
                parameterList = listOf(qrIdParameter),
                kind = McpToolKind.READ,
            ) { arguments -> appActionCall { qrAppAction.get(qrId = arguments.idText("qrId")) }?.toJson() ?: JsonNull },
            McpTool(
                name = "qr_add",
                description =
                    "Adds a QR code. The title must not be blank and the value must not be empty. " +
                        "The value is the exact text encoded in the QR, such as a URL, plain text, a WIFI: string or a vCard.",
                parameterList =
                    listOf(
                        McpToolParameter(name = TITLE, type = McpToolParameterType.STRING, description = "Title of the QR.", isRequired = true),
                        McpToolParameter(name = DESCRIPTION, type = McpToolParameterType.STRING, description = "Description of the QR."),
                        McpToolParameter(name = VALUE, type = McpToolParameterType.STRING, description = "Text encoded in the QR.", isRequired = true),
                        tagIdsParameter,
                    ),
                kind = McpToolKind.WRITE,
            ) { arguments ->
                appActionCall {
                    qrAppAction.add(
                        title = arguments.string(TITLE),
                        description = arguments.stringOrNull(DESCRIPTION),
                        value = arguments.string(VALUE),
                        tagIds = arguments.idTextList("tagIds"),
                    )
                }.toJson()
            },
            McpTool(
                name = "qr_update",
                description =
                    "Updates the title, description or value of a QR code. Omitted fields keep their current value. " +
                        "A blank title keeps the current title. The value must not be empty. Tags are changed with qr_tag_add and qr_tag_remove.",
                parameterList =
                    listOf(
                        qrIdParameter,
                        McpToolParameter(name = TITLE, type = McpToolParameterType.STRING, description = "New title."),
                        McpToolParameter(name = DESCRIPTION, type = McpToolParameterType.STRING, description = "New description."),
                        McpToolParameter(name = VALUE, type = McpToolParameterType.STRING, description = "New text encoded in the QR."),
                    ),
                kind = McpToolKind.WRITE,
            ) { arguments -> updateQr(arguments) },
            McpTool(
                name = "qr_delete",
                description = "Deletes a QR code. Use qr_restore with the same id to undo.",
                parameterList = listOf(qrIdParameter),
                kind = McpToolKind.DESTRUCTIVE,
            ) { arguments -> appActionCall { qrAppAction.delete(qrId = arguments.idText("qrId")) }.toJson() },
            McpTool(
                name = "qr_restore",
                description = "Restores a deleted QR code.",
                parameterList = listOf(qrIdParameter),
                kind = McpToolKind.WRITE,
            ) { arguments -> appActionCall { qrAppAction.restore(qrId = arguments.idText("qrId")) }.toJson() },
        ) +
            entityTagToolList(
                toolNamePrefix = "qr",
                entityName = "a QR code",
                kind = AppActionEntityTagKind.QR,
                entityIdParameter = qrIdParameter,
                entityTagAppAction = entityTagAppAction,
            )

    private suspend fun updateQr(arguments: McpToolArguments): JsonElement {
        val qrId = arguments.uuid("qrId")
        val current = findQrUseCase.readActive(id = qrId, isDeleted = Qr::isDeleted)?.detail ?: return changedCountResult(count = 0)
        val detail =
            QrDetail(
                title = arguments.stringOrNull(TITLE) ?: current.title,
                description = arguments.stringOrNull(DESCRIPTION) ?: current.description,
                value = arguments.stringOrNull(VALUE) ?: current.value,
            )

        return changedCountResult(updateQrUseCase(parameter = UpdateQrUseCase.Parameter(id = qrId, detail = detail)).getOrThrow())
    }
}

internal fun AppActionQr.toJson(): JsonObject =
    buildJsonObject {
        put("id", id)
        put(TITLE, title)
        put(DESCRIPTION, description)
        put(VALUE, value)
        put("createdAt", createdAt)
        put("updatedAt", updatedAt)
    }

private fun AppActionQrDetail.toJson(): JsonObject =
    buildJsonObject {
        put("qr", qr.toJson())
        put("tags", tags.toJsonArray { tag -> tag.toJson() })
    }
