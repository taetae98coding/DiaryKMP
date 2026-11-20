package io.github.taetae98coding.diary.work.mcp.tool.memo

import io.github.taetae98coding.diary.work.appaction.core.AppActionDone
import io.github.taetae98coding.diary.work.appaction.memolink.MemoLinkAppAction
import io.github.taetae98coding.diary.work.mcp.tool.McpTool
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArguments
import io.github.taetae98coding.diary.work.mcp.tool.McpToolKind
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameter
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameterType
import io.github.taetae98coding.diary.work.mcp.tool.McpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.appActionCall
import io.github.taetae98coding.diary.work.mcp.tool.idText
import io.github.taetae98coding.diary.work.mcp.tool.json.toJson
import io.github.taetae98coding.diary.work.mcp.tool.tag.NOT_DONE_DESCRIPTION
import org.koin.core.annotation.Factory

private const val TAG_ID = "tagId"

@Factory(binds = [McpToolSet::class])
internal class MemoLinkMcpToolSet(
    private val memoLinkAppAction: MemoLinkAppAction,
) : McpToolSet {
    override val toolList: List<McpTool> =
        listOf(
            linkTool(name = "memo_tag_add", description = "Links a tag to a memo.", targetName = TAG_ID) { memoId, tagId -> memoLinkAppAction.tagAdd(memoId = memoId, tagId = tagId) },
            linkTool(name = "memo_tag_remove", description = "Unlinks a tag from a memo. If it is the primary tag, the primary tag is cleared too.", targetName = TAG_ID) { memoId, tagId ->
                memoLinkAppAction.tagRemove(memoId = memoId, tagId = tagId)
            },
            linkTool(name = "memo_primary_tag_set", description = "Sets the primary tag of a memo. The tag is linked too when it is not linked yet.", targetName = TAG_ID) { memoId, tagId ->
                memoLinkAppAction.primaryTagSet(memoId = memoId, tagId = tagId)
            },
            McpTool(
                name = "memo_primary_tag_unset",
                description = "Clears the primary tag of a memo. The tag stays linked. Returns done false and changes nothing when the memo does not exist or is deleted.",
                parameterList = listOf(memoIdParameter),
                kind = McpToolKind.WRITE,
            ) { arguments -> appActionCall { memoLinkAppAction.primaryTagUnset(memoId = arguments.idText("memoId")) }.toJson() },
            linkTool(name = "memo_place_add", description = "Links a place to a memo.", targetName = "placeId") { memoId, placeId -> memoLinkAppAction.placeAdd(memoId = memoId, placeId = placeId) },
            linkTool(name = "memo_place_remove", description = "Unlinks a place from a memo.", targetName = "placeId") { memoId, placeId -> memoLinkAppAction.placeRemove(memoId = memoId, placeId = placeId) },
            linkTool(name = "memo_web_add", description = "Links a web to a memo.", targetName = "webId") { memoId, webId -> memoLinkAppAction.webAdd(memoId = memoId, webId = webId) },
            linkTool(name = "memo_web_remove", description = "Unlinks a web from a memo.", targetName = "webId") { memoId, webId -> memoLinkAppAction.webRemove(memoId = memoId, webId = webId) },
            linkTool(name = "memo_contact_add", description = "Links a contact to a memo.", targetName = "contactId") { memoId, contactId -> memoLinkAppAction.contactAdd(memoId = memoId, contactId = contactId) },
            linkTool(name = "memo_contact_remove", description = "Unlinks a contact from a memo.", targetName = "contactId") { memoId, contactId ->
                memoLinkAppAction.contactRemove(memoId = memoId, contactId = contactId)
            },
        )

    private fun linkTool(
        name: String,
        description: String,
        targetName: String,
        link: suspend (memoId: String, targetId: String) -> AppActionDone,
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
            appActionCall { link(arguments.idText("memoId"), arguments.idText(targetName)) }.toJson()
        }
}
