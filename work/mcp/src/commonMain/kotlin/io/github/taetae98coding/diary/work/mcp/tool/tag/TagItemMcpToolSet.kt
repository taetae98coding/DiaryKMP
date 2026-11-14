package io.github.taetae98coding.diary.work.mcp.tool.tag

import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.domain.checklist.usecase.PageTagChecklistUseCase
import io.github.taetae98coding.diary.domain.contact.usecase.PageTagContactUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.PageTagDDayUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageFinishedTagMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageTagMemoUseCase
import io.github.taetae98coding.diary.domain.place.usecase.PageTagPlaceUseCase
import io.github.taetae98coding.diary.domain.playlist.usecase.PageTagMusicUseCase
import io.github.taetae98coding.diary.domain.qr.usecase.PageTagQrUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.PageFinishedTagRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.PageTagRoutineUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.FindTagUseCase
import io.github.taetae98coding.diary.domain.web.usecase.PageTagWebUseCase
import io.github.taetae98coding.diary.work.mcp.tool.McpTool
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArgumentException
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArguments
import io.github.taetae98coding.diary.work.mcp.tool.McpToolKind
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameter
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameterType
import io.github.taetae98coding.diary.work.mcp.tool.McpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.checklist.toJson
import io.github.taetae98coding.diary.work.mcp.tool.contact.contactSortList
import io.github.taetae98coding.diary.work.mcp.tool.dday.toJson
import io.github.taetae98coding.diary.work.mcp.tool.json.sort
import io.github.taetae98coding.diary.work.mcp.tool.json.sortParameter
import io.github.taetae98coding.diary.work.mcp.tool.json.toJson
import io.github.taetae98coding.diary.work.mcp.tool.memo.memoSortList
import io.github.taetae98coding.diary.work.mcp.tool.music.musicSortList
import io.github.taetae98coding.diary.work.mcp.tool.music.toJson
import io.github.taetae98coding.diary.work.mcp.tool.pageActive
import io.github.taetae98coding.diary.work.mcp.tool.paging.mcpPageCursorParameter
import io.github.taetae98coding.diary.work.mcp.tool.paging.pageOffset
import io.github.taetae98coding.diary.work.mcp.tool.paging.readPage
import io.github.taetae98coding.diary.work.mcp.tool.paging.toJson
import io.github.taetae98coding.diary.work.mcp.tool.qr.toJson
import io.github.taetae98coding.diary.work.mcp.tool.routine.toJson
import io.github.taetae98coding.diary.work.mcp.tool.uuid
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.serialization.json.JsonObject
import org.koin.core.annotation.Factory
import kotlin.time.Clock
import kotlin.uuid.Uuid

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

private fun McpToolArguments.scope(): TagScope {
    val argument = stringOrNull(SCOPE) ?: return TagScope.SELF

    return TagScope.entries.firstOrNull { scope -> scope.toArgument() == argument }
        ?: throw McpToolArgumentException(message = "'$SCOPE' must be one of ${tagScopeList.joinToString { scope -> scope.toArgument() }}.")
}

@Factory
internal class TagItemMcpToolSet(
    private val findTagUseCase: FindTagUseCase,
    private val pageTagMemoUseCase: PageTagMemoUseCase,
    private val pageFinishedTagMemoUseCase: PageFinishedTagMemoUseCase,
    private val pageTagWebUseCase: PageTagWebUseCase,
    private val pageTagPlaceUseCase: PageTagPlaceUseCase,
    private val pageTagContactUseCase: PageTagContactUseCase,
    private val pageTagChecklistUseCase: PageTagChecklistUseCase,
    private val pageTagMusicUseCase: PageTagMusicUseCase,
    private val pageTagQrUseCase: PageTagQrUseCase,
    private val pageTagDDayUseCase: PageTagDDayUseCase,
    private val pageTagRoutineUseCase: PageTagRoutineUseCase,
    private val pageFinishedTagRoutineUseCase: PageFinishedTagRoutineUseCase,
    private val clock: Clock,
) : McpToolSet {
    override val toolList: List<McpTool> =
        listOf(
            McpTool(
                name = "tag_memo_list",
                description = "Lists memos linked to a tag that are neither finished nor deleted. A memo linked to several included tags appears once.",
                parameterList = listOf(tagIdParameter, scopeParameter, sortParameter(memoSortList), mcpPageCursorParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                val parameter = PageTagMemoUseCase.Parameter(tagId = arguments.uuid(TAG_ID), scope = arguments.scope(), sort = arguments.sort(memoSortList))

                pageOfTag(tagId = parameter.tagId, arguments = arguments) { offset ->
                    pageTagMemoUseCase.readPage(parameter = parameter, offset = offset).toJson { memo -> memo.toJson() }
                }
            },
            McpTool(
                name = "tag_memo_finished_list",
                description = "Lists finished memos linked to a tag that are not deleted. Only memos linked to this tag itself are included.",
                parameterList = listOf(tagIdParameter, sortParameter(memoSortList), mcpPageCursorParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                val parameter = PageFinishedTagMemoUseCase.Parameter(tagId = arguments.uuid(TAG_ID), sort = arguments.sort(memoSortList))

                pageOfTag(tagId = parameter.tagId, arguments = arguments) { offset ->
                    pageFinishedTagMemoUseCase.readPage(parameter = parameter, offset = offset).toJson { memo -> memo.toJson() }
                }
            },
            McpTool(
                name = "tag_web_list",
                description = "Lists webs linked to a tag that are not deleted. A web linked to several included tags appears once.",
                parameterList = listOf(tagIdParameter, scopeParameter, sortParameter(tagSortList), mcpPageCursorParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                val parameter = PageTagWebUseCase.Parameter(tagId = arguments.uuid(TAG_ID), scope = arguments.scope(), sort = arguments.sort(tagSortList))

                pageOfTag(tagId = parameter.tagId, arguments = arguments) { offset ->
                    pageTagWebUseCase.readPage(parameter = parameter, offset = offset).toJson { web -> web.toJson() }
                }
            },
            McpTool(
                name = "tag_place_list",
                description = "Lists places linked to a tag that are not deleted. A place linked to several included tags appears once.",
                parameterList = listOf(tagIdParameter, scopeParameter, sortParameter(tagSortList), mcpPageCursorParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                val parameter = PageTagPlaceUseCase.Parameter(tagId = arguments.uuid(TAG_ID), scope = arguments.scope(), sort = arguments.sort(tagSortList))

                pageOfTag(tagId = parameter.tagId, arguments = arguments) { offset ->
                    pageTagPlaceUseCase.readPage(parameter = parameter, offset = offset).toJson { place -> place.toJson() }
                }
            },
            McpTool(
                name = "tag_contact_list",
                description =
                    "Lists contacts linked to a tag that are not deleted. A contact linked to several included tags appears once. " +
                        "Favorite contacts always come first, and the sort applies within favorites and within the rest.",
                parameterList = listOf(tagIdParameter, scopeParameter, sortParameter(contactSortList), mcpPageCursorParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                val parameter = PageTagContactUseCase.Parameter(tagId = arguments.uuid(TAG_ID), scope = arguments.scope(), sort = arguments.sort(contactSortList))

                pageOfTag(tagId = parameter.tagId, arguments = arguments) { offset ->
                    pageTagContactUseCase.readPage(parameter = parameter, offset = offset).toJson { contact -> contact.toJson() }
                }
            },
            McpTool(
                name = "tag_checklist_list",
                description =
                    "Lists checklists linked to a tag that are not deleted, by title, with how many of their items are checked. " +
                        "A checklist linked to several included tags appears once.",
                parameterList = listOf(tagIdParameter, scopeParameter, mcpPageCursorParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                val parameter = PageTagChecklistUseCase.Parameter(tagId = arguments.uuid(TAG_ID), scope = arguments.scope())

                pageOfTag(tagId = parameter.tagId, arguments = arguments) { offset ->
                    pageTagChecklistUseCase.readPage(parameter = parameter, offset = offset).toJson { summary -> summary.toJson() }
                }
            },
            McpTool(
                name = "tag_music_list",
                description = "Lists the music linked to a tag that is not deleted. Music linked to several included tags appears once.",
                parameterList = listOf(tagIdParameter, scopeParameter, sortParameter(musicSortList), mcpPageCursorParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                val parameter = PageTagMusicUseCase.Parameter(tagId = arguments.uuid(TAG_ID), scope = arguments.scope(), sort = arguments.sort(musicSortList))

                pageOfTag(tagId = parameter.tagId, arguments = arguments) { offset ->
                    pageTagMusicUseCase.readPage(parameter = parameter, offset = offset).toJson { music -> music.toJson() }
                }
            },
            McpTool(
                name = "tag_qr_list",
                description = "Lists QR codes linked to a tag that are not deleted, ordered by title. A QR code linked to several included tags appears once.",
                parameterList = listOf(tagIdParameter, scopeParameter, mcpPageCursorParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                val parameter = PageTagQrUseCase.Parameter(tagId = arguments.uuid(TAG_ID), scope = arguments.scope())

                pageOfTag(tagId = parameter.tagId, arguments = arguments) { offset ->
                    pageTagQrUseCase.readPage(parameter = parameter, offset = offset).toJson { qr -> qr.toJson() }
                }
            },
            McpTool(
                name = "tag_dday_list",
                description =
                    "Lists D-Days linked to a tag that are not deleted, by title, with the days left or passed counted from today. " +
                        "A D-Day linked to several included tags appears once.",
                parameterList = listOf(tagIdParameter, scopeParameter, mcpPageCursorParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                val parameter = PageTagDDayUseCase.Parameter(tagId = arguments.uuid(TAG_ID), scope = arguments.scope())

                pageOfTag(tagId = parameter.tagId, arguments = arguments) { offset ->
                    val today = clock.todayIn(TimeZone.currentSystemDefault())

                    pageTagDDayUseCase.readPage(parameter = parameter, offset = offset).toJson { dday -> dday.toJson(today = today) }
                }
            },
            McpTool(
                name = "tag_routine_list",
                description =
                    "Lists routines linked to a tag that are not finished and not deleted, in progress first, then not started, by title within each, " +
                        "with this cycle's total and achievement rate counted from today. A routine linked to several included tags appears once.",
                parameterList = listOf(tagIdParameter, scopeParameter, mcpPageCursorParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                val tagId = arguments.uuid(TAG_ID)
                val scope = arguments.scope()

                pageOfTag(tagId = tagId, arguments = arguments) { offset ->
                    val today = clock.todayIn(TimeZone.currentSystemDefault())
                    val parameter = PageTagRoutineUseCase.Parameter(tagId = tagId, scope = scope, today = today)

                    pageTagRoutineUseCase.readPage(parameter = parameter, offset = offset).toJson { summary -> summary.toJson(today = today) }
                }
            },
            McpTool(
                name = "tag_routine_finished_list",
                description =
                    "Lists finished routines linked to a tag that are not deleted, in progress first, then not started, by title within each. " +
                        "Only routines linked to this tag itself are included.",
                parameterList = listOf(tagIdParameter, mcpPageCursorParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                val tagId = arguments.uuid(TAG_ID)

                pageOfTag(tagId = tagId, arguments = arguments) { offset ->
                    val today = clock.todayIn(TimeZone.currentSystemDefault())
                    val parameter = PageFinishedTagRoutineUseCase.Parameter(tagId = tagId, today = today)

                    pageFinishedTagRoutineUseCase.readPage(parameter = parameter, offset = offset).toJson { summary -> summary.toJson(today = today) }
                }
            },
        )

    private suspend fun pageOfTag(
        tagId: Uuid,
        arguments: McpToolArguments,
        page: suspend (offset: Int) -> JsonObject,
    ): JsonObject {
        val offset = arguments.pageOffset()

        return findTagUseCase.pageActive(id = tagId, isDeleted = Tag::isDeleted) { page(offset) }
    }
}
