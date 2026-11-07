package io.github.taetae98coding.diary.work.mcp.tool.memo

import io.github.taetae98coding.diary.domain.memo.usecase.AddMemoContactUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.AddMemoPlaceUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.AddMemoTagUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.AddMemoWebUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RemoveMemoContactUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RemoveMemoPlaceUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RemoveMemoTagUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RemoveMemoWebUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.SetMemoPrimaryTagUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.UnsetMemoPrimaryTagUseCase
import io.github.taetae98coding.diary.work.mcp.tool.McpTool
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArguments
import io.github.taetae98coding.diary.work.mcp.tool.McpToolKind
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameter
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameterType
import io.github.taetae98coding.diary.work.mcp.tool.McpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.json.doneResult
import io.github.taetae98coding.diary.work.mcp.tool.uuid
import kotlinx.serialization.json.JsonElement
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

private const val TAG_ID = "tagId"

@Factory
internal class MemoLinkMcpToolSet(
    private val addMemoTagUseCase: AddMemoTagUseCase,
    private val removeMemoTagUseCase: RemoveMemoTagUseCase,
    private val setMemoPrimaryTagUseCase: SetMemoPrimaryTagUseCase,
    private val unsetMemoPrimaryTagUseCase: UnsetMemoPrimaryTagUseCase,
    private val addMemoPlaceUseCase: AddMemoPlaceUseCase,
    private val removeMemoPlaceUseCase: RemoveMemoPlaceUseCase,
    private val addMemoWebUseCase: AddMemoWebUseCase,
    private val removeMemoWebUseCase: RemoveMemoWebUseCase,
    private val addMemoContactUseCase: AddMemoContactUseCase,
    private val removeMemoContactUseCase: RemoveMemoContactUseCase,
) : McpToolSet {
    override val toolList: List<McpTool> =
        listOf(
            linkTool(name = "memo_tag_add", description = "Links a tag to a memo.", targetName = TAG_ID) { memoId, tagId ->
                addMemoTagUseCase(parameter = AddMemoTagUseCase.Parameter(memoId = memoId, tagId = tagId)).getOrThrow()
            },
            linkTool(
                name = "memo_tag_remove",
                description = "Unlinks a tag from a memo. If it is the primary tag, the primary tag is cleared too.",
                targetName = TAG_ID,
            ) { memoId, tagId ->
                removeMemoTagUseCase(parameter = RemoveMemoTagUseCase.Parameter(memoId = memoId, tagId = tagId)).getOrThrow()
            },
            linkTool(
                name = "memo_primary_tag_set",
                description = "Sets the primary tag of a memo. The tag is linked too when it is not linked yet.",
                targetName = TAG_ID,
            ) { memoId, tagId ->
                setMemoPrimaryTagUseCase(parameter = SetMemoPrimaryTagUseCase.Parameter(memoId = memoId, tagId = tagId)).getOrThrow()
            },
            McpTool(
                name = "memo_primary_tag_unset",
                description = "Clears the primary tag of a memo. The tag stays linked.",
                parameterList = listOf(memoIdParameter),
                kind = McpToolKind.WRITE,
            ) { arguments ->
                unsetMemoPrimaryTagUseCase(parameter = arguments.uuid("memoId")).getOrThrow()
                doneResult
            },
            linkTool(name = "memo_place_add", description = "Links a place to a memo.", targetName = "placeId") { memoId, placeId ->
                addMemoPlaceUseCase(parameter = AddMemoPlaceUseCase.Parameter(memoId = memoId, placeId = placeId)).getOrThrow()
            },
            linkTool(name = "memo_place_remove", description = "Unlinks a place from a memo.", targetName = "placeId") { memoId, placeId ->
                removeMemoPlaceUseCase(parameter = RemoveMemoPlaceUseCase.Parameter(memoId = memoId, placeId = placeId)).getOrThrow()
            },
            linkTool(name = "memo_web_add", description = "Links a web to a memo.", targetName = "webId") { memoId, webId ->
                addMemoWebUseCase(parameter = AddMemoWebUseCase.Parameter(memoId = memoId, webId = webId)).getOrThrow()
            },
            linkTool(name = "memo_web_remove", description = "Unlinks a web from a memo.", targetName = "webId") { memoId, webId ->
                removeMemoWebUseCase(parameter = RemoveMemoWebUseCase.Parameter(memoId = memoId, webId = webId)).getOrThrow()
            },
            linkTool(name = "memo_contact_add", description = "Links a contact to a memo.", targetName = "contactId") { memoId, contactId ->
                addMemoContactUseCase(parameter = AddMemoContactUseCase.Parameter(memoId = memoId, contactId = contactId)).getOrThrow()
            },
            linkTool(name = "memo_contact_remove", description = "Unlinks a contact from a memo.", targetName = "contactId") { memoId, contactId ->
                removeMemoContactUseCase(parameter = RemoveMemoContactUseCase.Parameter(memoId = memoId, contactId = contactId)).getOrThrow()
            },
        )

    private fun linkTool(
        name: String,
        description: String,
        targetName: String,
        link: suspend (memoId: Uuid, targetId: Uuid) -> Unit,
    ): McpTool =
        McpTool(
            name = name,
            description = description,
            parameterList =
                listOf(
                    memoIdParameter,
                    McpToolParameter(name = targetName, type = McpToolParameterType.STRING, description = "Id of the linked item.", isRequired = true),
                ),
            kind = McpToolKind.WRITE,
        ) { arguments: McpToolArguments ->
            link(arguments.uuid("memoId"), arguments.uuid(targetName))
            doneResult as JsonElement
        }
}
