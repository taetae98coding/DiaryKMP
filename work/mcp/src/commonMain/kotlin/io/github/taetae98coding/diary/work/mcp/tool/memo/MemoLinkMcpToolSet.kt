package io.github.taetae98coding.diary.work.mcp.tool.memo

import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.domain.contact.usecase.FindContactUseCase
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.AddMemoContactUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.AddMemoPlaceUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.AddMemoTagUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.AddMemoWebUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.FindMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RemoveMemoContactUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RemoveMemoPlaceUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RemoveMemoTagUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RemoveMemoWebUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.SetMemoPrimaryTagUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.UnsetMemoPrimaryTagUseCase
import io.github.taetae98coding.diary.domain.place.usecase.FindPlaceUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.FindTagUseCase
import io.github.taetae98coding.diary.domain.web.usecase.FindWebUseCase
import io.github.taetae98coding.diary.work.mcp.tool.McpTool
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArguments
import io.github.taetae98coding.diary.work.mcp.tool.McpToolKind
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameter
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameterType
import io.github.taetae98coding.diary.work.mcp.tool.McpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.isActive
import io.github.taetae98coding.diary.work.mcp.tool.json.doneResult
import io.github.taetae98coding.diary.work.mcp.tool.json.notDoneResult
import io.github.taetae98coding.diary.work.mcp.tool.tag.NOT_DONE_DESCRIPTION
import io.github.taetae98coding.diary.work.mcp.tool.uuid
import kotlinx.serialization.json.JsonElement
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

private const val TAG_ID = "tagId"

@Factory
internal class MemoLinkMcpToolSet(
    private val findMemoUseCase: FindMemoUseCase,
    private val findTagUseCase: FindTagUseCase,
    private val findPlaceUseCase: FindPlaceUseCase,
    private val findWebUseCase: FindWebUseCase,
    private val findContactUseCase: FindContactUseCase,
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
            linkTool(name = "memo_tag_add", description = "Links a tag to a memo.", targetName = TAG_ID, findTargetUseCase = findTagUseCase, isTargetDeleted = Tag::isDeleted) { memoId, tagId ->
                addMemoTagUseCase(parameter = AddMemoTagUseCase.Parameter(memoId = memoId, tagId = tagId)).getOrThrow()
            },
            linkTool(
                name = "memo_tag_remove",
                description = "Unlinks a tag from a memo. If it is the primary tag, the primary tag is cleared too.",
                targetName = TAG_ID,
                findTargetUseCase = findTagUseCase,
                isTargetDeleted = Tag::isDeleted,
            ) { memoId, tagId ->
                removeMemoTagUseCase(parameter = RemoveMemoTagUseCase.Parameter(memoId = memoId, tagId = tagId)).getOrThrow()
            },
            linkTool(
                name = "memo_primary_tag_set",
                description = "Sets the primary tag of a memo. The tag is linked too when it is not linked yet.",
                targetName = TAG_ID,
                findTargetUseCase = findTagUseCase,
                isTargetDeleted = Tag::isDeleted,
            ) { memoId, tagId ->
                setMemoPrimaryTagUseCase(parameter = SetMemoPrimaryTagUseCase.Parameter(memoId = memoId, tagId = tagId)).getOrThrow()
            },
            McpTool(
                name = "memo_primary_tag_unset",
                description = "Clears the primary tag of a memo. The tag stays linked. Returns done false and changes nothing when the memo does not exist or is deleted.",
                parameterList = listOf(memoIdParameter),
                kind = McpToolKind.WRITE,
            ) { arguments -> unsetPrimaryTag(memoId = arguments.uuid("memoId")) },
            linkTool(name = "memo_place_add", description = "Links a place to a memo.", targetName = "placeId", findTargetUseCase = findPlaceUseCase, isTargetDeleted = Place::isDeleted) { memoId, placeId ->
                addMemoPlaceUseCase(parameter = AddMemoPlaceUseCase.Parameter(memoId = memoId, placeId = placeId)).getOrThrow()
            },
            linkTool(name = "memo_place_remove", description = "Unlinks a place from a memo.", targetName = "placeId", findTargetUseCase = findPlaceUseCase, isTargetDeleted = Place::isDeleted) { memoId, placeId ->
                removeMemoPlaceUseCase(parameter = RemoveMemoPlaceUseCase.Parameter(memoId = memoId, placeId = placeId)).getOrThrow()
            },
            linkTool(name = "memo_web_add", description = "Links a web to a memo.", targetName = "webId", findTargetUseCase = findWebUseCase, isTargetDeleted = Web::isDeleted) { memoId, webId ->
                addMemoWebUseCase(parameter = AddMemoWebUseCase.Parameter(memoId = memoId, webId = webId)).getOrThrow()
            },
            linkTool(name = "memo_web_remove", description = "Unlinks a web from a memo.", targetName = "webId", findTargetUseCase = findWebUseCase, isTargetDeleted = Web::isDeleted) { memoId, webId ->
                removeMemoWebUseCase(parameter = RemoveMemoWebUseCase.Parameter(memoId = memoId, webId = webId)).getOrThrow()
            },
            linkTool(name = "memo_contact_add", description = "Links a contact to a memo.", targetName = "contactId", findTargetUseCase = findContactUseCase, isTargetDeleted = Contact::isDeleted) { memoId, contactId ->
                addMemoContactUseCase(parameter = AddMemoContactUseCase.Parameter(memoId = memoId, contactId = contactId)).getOrThrow()
            },
            linkTool(name = "memo_contact_remove", description = "Unlinks a contact from a memo.", targetName = "contactId", findTargetUseCase = findContactUseCase, isTargetDeleted = Contact::isDeleted) { memoId, contactId ->
                removeMemoContactUseCase(parameter = RemoveMemoContactUseCase.Parameter(memoId = memoId, contactId = contactId)).getOrThrow()
            },
        )

    private suspend fun unsetPrimaryTag(memoId: Uuid): JsonElement {
        if (!findMemoUseCase.isActive(id = memoId, isDeleted = Memo::isDeleted)) return notDoneResult

        unsetMemoPrimaryTagUseCase(parameter = memoId).getOrThrow()

        return doneResult
    }

    private fun <T : Any> linkTool(
        name: String,
        description: String,
        targetName: String,
        findTargetUseCase: FlowUseCase<Uuid, T?>,
        isTargetDeleted: (T) -> Boolean,
        link: suspend (memoId: Uuid, targetId: Uuid) -> Unit,
    ): McpTool =
        McpTool(
            name = name,
            description = "$description $NOT_DONE_DESCRIPTION",
            parameterList =
                listOf(
                    memoIdParameter,
                    McpToolParameter(name = targetName, type = McpToolParameterType.STRING, description = "Id of the linked item.", isRequired = true),
                ),
            kind = McpToolKind.WRITE,
        ) { arguments: McpToolArguments ->
            val memoId = arguments.uuid("memoId")
            val targetId = arguments.uuid(targetName)
            val isLinkable = findMemoUseCase.isActive(id = memoId, isDeleted = Memo::isDeleted) && findTargetUseCase.isActive(id = targetId, isDeleted = isTargetDeleted)

            if (isLinkable) {
                link(memoId, targetId)
                doneResult
            } else {
                notDoneResult
            }
        }
}
