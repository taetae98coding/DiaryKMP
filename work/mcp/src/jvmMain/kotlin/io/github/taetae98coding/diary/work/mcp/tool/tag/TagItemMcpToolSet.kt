package io.github.taetae98coding.diary.work.mcp.tool.tag

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.work.appaction.tag.TagItemAppAction
import io.github.taetae98coding.diary.work.mcp.tool.McpTool
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArgumentException
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArguments
import io.github.taetae98coding.diary.work.mcp.tool.McpToolKind
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameter
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameterType
import io.github.taetae98coding.diary.work.mcp.tool.McpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.appActionCall
import io.github.taetae98coding.diary.work.mcp.tool.checklist.toJson
import io.github.taetae98coding.diary.work.mcp.tool.contact.contactSortList
import io.github.taetae98coding.diary.work.mcp.tool.cursorText
import io.github.taetae98coding.diary.work.mcp.tool.dday.toJson
import io.github.taetae98coding.diary.work.mcp.tool.idText
import io.github.taetae98coding.diary.work.mcp.tool.json.sortParameter
import io.github.taetae98coding.diary.work.mcp.tool.json.toJson
import io.github.taetae98coding.diary.work.mcp.tool.memo.memoSortList
import io.github.taetae98coding.diary.work.mcp.tool.music.musicSortList
import io.github.taetae98coding.diary.work.mcp.tool.music.toJson
import io.github.taetae98coding.diary.work.mcp.tool.paging.mcpPageCursorParameter
import io.github.taetae98coding.diary.work.mcp.tool.paging.toJson
import io.github.taetae98coding.diary.work.mcp.tool.qr.toJson
import io.github.taetae98coding.diary.work.mcp.tool.routine.toJson
import io.github.taetae98coding.diary.work.mcp.tool.sortText

private const val TAG_ID = "tagId"

private const val SCOPE = "scope"

internal val tagScopeList: List<TagScope> = listOf(TagScope.SELF, TagScope.CHILD, TagScope.DESCENDANT)

private fun TagScope.toArgument(): String =
    when (this) {
        TagScope.SELF -> "self"
        TagScope.CHILD -> "child"
        TagScope.DESCENDANT -> "descendant"
    }

private val scopeParameter: McpToolParameter =
    McpToolParameter(
        name = SCOPE,
        type = McpToolParameterType.STRING,
        description =
            "Which tags to include. self: only this tag. child: this tag and the tags it links to directly. " +
                "descendant: this tag and every tag reachable through its links. Omit it to use self.",
        allowedValueList = tagScopeList.map { scope -> scope.toArgument() },
    )

private fun McpToolArguments.scopeText(): String {
    val argument = stringOrNull(SCOPE) ?: return TagScope.SELF.toArgument()

    return tagScopeList.firstOrNull { scope -> scope.toArgument() == argument }?.toArgument()
        ?: throw McpToolArgumentException(message = "'$SCOPE' must be one of ${tagScopeList.joinToString { scope -> scope.toArgument() }}.")
}

@Inject
@ContributesIntoSet(AppScope::class, binding = binding<McpToolSet>())
internal class TagItemMcpToolSet(
    private val tagItemAppAction: TagItemAppAction,
) : McpToolSet {
    override val toolList: List<McpTool> =
        listOf(
            McpTool(
                name = "tag_memo_list",
                description = "Lists memos linked to a tag that are neither finished nor deleted. A memo linked to several included tags appears once.",
                parameterList = listOf(tagIdParameter, scopeParameter, sortParameter(memoSortList), mcpPageCursorParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                appActionCall { tagItemAppAction.memoList(tagId = arguments.idText(TAG_ID), scope = arguments.scopeText(), sort = arguments.sortText(memoSortList), cursor = arguments.cursorText()) }.toJson { memo -> memo.toJson() }
            },
            McpTool(
                name = "tag_memo_finished_list",
                description = "Lists finished memos linked to a tag that are not deleted. Only memos linked to this tag itself are included.",
                parameterList = listOf(tagIdParameter, sortParameter(memoSortList), mcpPageCursorParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                appActionCall { tagItemAppAction.memoFinishedList(tagId = arguments.idText(TAG_ID), sort = arguments.sortText(memoSortList), cursor = arguments.cursorText()) }.toJson { memo -> memo.toJson() }
            },
            McpTool(
                name = "tag_web_list",
                description = "Lists webs linked to a tag that are not deleted. A web linked to several included tags appears once.",
                parameterList = listOf(tagIdParameter, scopeParameter, sortParameter(tagSortList), mcpPageCursorParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                appActionCall { tagItemAppAction.webList(tagId = arguments.idText(TAG_ID), scope = arguments.scopeText(), sort = arguments.sortText(tagSortList), cursor = arguments.cursorText()) }.toJson { web -> web.toJson() }
            },
            McpTool(
                name = "tag_place_list",
                description = "Lists places linked to a tag that are not deleted. A place linked to several included tags appears once.",
                parameterList = listOf(tagIdParameter, scopeParameter, sortParameter(tagSortList), mcpPageCursorParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                appActionCall { tagItemAppAction.placeList(tagId = arguments.idText(TAG_ID), scope = arguments.scopeText(), sort = arguments.sortText(tagSortList), cursor = arguments.cursorText()) }.toJson { place -> place.toJson() }
            },
            McpTool(
                name = "tag_contact_list",
                description =
                    "Lists contacts linked to a tag that are not deleted. A contact linked to several included tags appears once. " +
                        "Favorite contacts always come first, and the sort applies within favorites and within the rest.",
                parameterList = listOf(tagIdParameter, scopeParameter, sortParameter(contactSortList), mcpPageCursorParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                appActionCall { tagItemAppAction.contactList(tagId = arguments.idText(TAG_ID), scope = arguments.scopeText(), sort = arguments.sortText(contactSortList), cursor = arguments.cursorText()) }.toJson { contact -> contact.toJson() }
            },
            McpTool(
                name = "tag_checklist_list",
                description =
                    "Lists checklists linked to a tag that are not deleted, by title, with how many of their items are checked. " +
                        "A checklist linked to several included tags appears once.",
                parameterList = listOf(tagIdParameter, scopeParameter, mcpPageCursorParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                appActionCall { tagItemAppAction.checklistList(tagId = arguments.idText(TAG_ID), scope = arguments.scopeText(), cursor = arguments.cursorText()) }.toJson { summary -> summary.toJson() }
            },
            McpTool(
                name = "tag_music_list",
                description = "Lists the music linked to a tag that is not deleted. Music linked to several included tags appears once.",
                parameterList = listOf(tagIdParameter, scopeParameter, sortParameter(musicSortList), mcpPageCursorParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                appActionCall { tagItemAppAction.musicList(tagId = arguments.idText(TAG_ID), scope = arguments.scopeText(), sort = arguments.sortText(musicSortList), cursor = arguments.cursorText()) }.toJson { music -> music.toJson() }
            },
            McpTool(
                name = "tag_qr_list",
                description = "Lists QR codes linked to a tag that are not deleted, ordered by title. A QR code linked to several included tags appears once.",
                parameterList = listOf(tagIdParameter, scopeParameter, mcpPageCursorParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                appActionCall { tagItemAppAction.qrList(tagId = arguments.idText(TAG_ID), scope = arguments.scopeText(), cursor = arguments.cursorText()) }.toJson { qr -> qr.toJson() }
            },
            McpTool(
                name = "tag_dday_list",
                description =
                    "Lists D-Days linked to a tag that are not deleted, by title, with the days left or passed counted from today. " +
                        "A D-Day linked to several included tags appears once.",
                parameterList = listOf(tagIdParameter, scopeParameter, mcpPageCursorParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                appActionCall { tagItemAppAction.ddayList(tagId = arguments.idText(TAG_ID), scope = arguments.scopeText(), cursor = arguments.cursorText()) }.toJson { dday -> dday.toJson() }
            },
            McpTool(
                name = "tag_routine_list",
                description =
                    "Lists routines linked to a tag that are not finished and not deleted, in progress first, then not started, by title within each, " +
                        "with this cycle's total and achievement rate counted from today. A routine linked to several included tags appears once.",
                parameterList = listOf(tagIdParameter, scopeParameter, mcpPageCursorParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                appActionCall { tagItemAppAction.routineList(tagId = arguments.idText(TAG_ID), scope = arguments.scopeText(), cursor = arguments.cursorText()) }.toJson { routine -> routine.toJson() }
            },
            McpTool(
                name = "tag_routine_finished_list",
                description =
                    "Lists finished routines linked to a tag that are not deleted, in progress first, then not started, by title within each. " +
                        "Only routines linked to this tag itself are included.",
                parameterList = listOf(tagIdParameter, mcpPageCursorParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                appActionCall { tagItemAppAction.routineFinishedList(tagId = arguments.idText(TAG_ID), cursor = arguments.cursorText()) }.toJson { routine -> routine.toJson() }
            },
        )
}
