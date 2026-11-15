package io.github.taetae98coding.diary.work.mcp.tool.qr

import io.github.taetae98coding.diary.core.model.qr.Qr
import io.github.taetae98coding.diary.core.model.qr.QrDetail
import io.github.taetae98coding.diary.domain.qr.usecase.AddQrUseCase
import io.github.taetae98coding.diary.domain.qr.usecase.DeleteQrUseCase
import io.github.taetae98coding.diary.domain.qr.usecase.FindQrUseCase
import io.github.taetae98coding.diary.domain.qr.usecase.PageQrUseCase
import io.github.taetae98coding.diary.domain.qr.usecase.RestoreQrUseCase
import io.github.taetae98coding.diary.domain.qr.usecase.UpdateQrUseCase
import io.github.taetae98coding.diary.domain.tag.EntityTagKind
import io.github.taetae98coding.diary.domain.tag.usecase.AddEntityTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.FindTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.GetEntityTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.RemoveEntityTagUseCase
import io.github.taetae98coding.diary.work.mcp.tool.McpTool
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArguments
import io.github.taetae98coding.diary.work.mcp.tool.McpToolKind
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameter
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameterType
import io.github.taetae98coding.diary.work.mcp.tool.McpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.changeActive
import io.github.taetae98coding.diary.work.mcp.tool.isActive
import io.github.taetae98coding.diary.work.mcp.tool.json.changedCountResult
import io.github.taetae98coding.diary.work.mcp.tool.json.idResult
import io.github.taetae98coding.diary.work.mcp.tool.json.put
import io.github.taetae98coding.diary.work.mcp.tool.json.toJson
import io.github.taetae98coding.diary.work.mcp.tool.json.toJsonArray
import io.github.taetae98coding.diary.work.mcp.tool.paging.mcpPageCursorParameter
import io.github.taetae98coding.diary.work.mcp.tool.paging.pageOffset
import io.github.taetae98coding.diary.work.mcp.tool.paging.readPage
import io.github.taetae98coding.diary.work.mcp.tool.paging.toJson
import io.github.taetae98coding.diary.work.mcp.tool.readActive
import io.github.taetae98coding.diary.work.mcp.tool.readFirst
import io.github.taetae98coding.diary.work.mcp.tool.tag.entityTagToolList
import io.github.taetae98coding.diary.work.mcp.tool.tag.tagIdsParameter
import io.github.taetae98coding.diary.work.mcp.tool.uuid
import io.github.taetae98coding.diary.work.mcp.tool.uuidSet
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.koin.core.annotation.Factory

private const val DESCRIPTION = "description"
private const val TITLE = "title"
private const val VALUE = "value"

private val qrIdParameter: McpToolParameter =
    McpToolParameter(name = "qrId", type = McpToolParameterType.STRING, description = "Id of the QR.", isRequired = true)

@Factory
internal class QrMcpToolSet(
    private val pageQrUseCase: PageQrUseCase,
    private val findQrUseCase: FindQrUseCase,
    private val getEntityTagUseCase: GetEntityTagUseCase,
    private val addQrUseCase: AddQrUseCase,
    private val updateQrUseCase: UpdateQrUseCase,
    private val deleteQrUseCase: DeleteQrUseCase,
    private val restoreQrUseCase: RestoreQrUseCase,
    private val findTagUseCase: FindTagUseCase,
    private val addEntityTagUseCase: AddEntityTagUseCase,
    private val removeEntityTagUseCase: RemoveEntityTagUseCase,
) : McpToolSet {
    override val toolList: List<McpTool> =
        listOf(
            McpTool(
                name = "qr_list",
                description = "Lists saved QR codes that are not deleted, ordered by title.",
                parameterList = listOf(mcpPageCursorParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                pageQrUseCase
                    .readPage(parameter = Unit, offset = arguments.pageOffset())
                    .toJson { qr -> qr.toJson() }
            },
            McpTool(
                name = "qr_get",
                description = "Gets one saved QR code with its linked tags. Returns null when the QR does not exist or is deleted.",
                parameterList = listOf(qrIdParameter),
                kind = McpToolKind.READ,
            ) { arguments -> getQr(arguments) },
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
                val id =
                    addQrUseCase(
                        parameter =
                            AddQrUseCase.Parameter(
                                detail =
                                    QrDetail(
                                        title = arguments.string(TITLE),
                                        description = arguments.stringOrNull(DESCRIPTION).orEmpty(),
                                        value = arguments.string(VALUE),
                                    ),
                                tagIdSet = arguments.uuidSet("tagIds"),
                            ),
                    ).getOrThrow()

                idResult(id)
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
            ) { arguments -> findQrUseCase.changeActive(id = arguments.uuid("qrId"), isDeleted = Qr::isDeleted) { id -> deleteQrUseCase(parameter = id) } },
            McpTool(
                name = "qr_restore",
                description = "Restores a deleted QR code.",
                parameterList = listOf(qrIdParameter),
                kind = McpToolKind.WRITE,
            ) { arguments -> changedCountResult(restoreQrUseCase(parameter = arguments.uuid("qrId")).getOrThrow()) },
        ) +
            entityTagToolList(
                toolNamePrefix = "qr",
                entityName = "a QR code",
                kind = EntityTagKind.QR,
                entityIdParameter = qrIdParameter,
                findTagUseCase = findTagUseCase,
                addEntityTagUseCase = addEntityTagUseCase,
                removeEntityTagUseCase = removeEntityTagUseCase,
            ) { id -> findQrUseCase.isActive(id = id, isDeleted = Qr::isDeleted) }

    private suspend fun getQr(arguments: McpToolArguments): JsonElement {
        val qrId = arguments.uuid("qrId")
        val qr = findQrUseCase.readFirst(parameter = qrId)?.takeUnless { qr -> qr.isDeleted } ?: return JsonNull

        return buildJsonObject {
            put("qr", qr.toJson())
            put("tags", getEntityTagUseCase.readFirst(parameter = GetEntityTagUseCase.Parameter(kind = EntityTagKind.QR, entityId = qrId)).toJsonArray { tag -> tag.toJson() })
        }
    }

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

internal fun Qr.toJson(): JsonObject =
    buildJsonObject {
        put("id", id)
        put(TITLE, detail.title)
        put(DESCRIPTION, detail.description)
        put(VALUE, detail.value)
        put("createdAt", createdAt)
        put("updatedAt", updatedAt)
    }
