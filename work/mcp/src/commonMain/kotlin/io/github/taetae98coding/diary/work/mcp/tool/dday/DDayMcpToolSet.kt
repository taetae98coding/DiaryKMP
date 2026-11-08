package io.github.taetae98coding.diary.work.mcp.tool.dday

import io.github.taetae98coding.diary.core.model.dday.DDayOverflowPolicy
import io.github.taetae98coding.diary.domain.dday.DDayInput
import io.github.taetae98coding.diary.domain.dday.repeat.toRepeatInputList
import io.github.taetae98coding.diary.domain.dday.usecase.AddDDayUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.DeleteDDayUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.FindDDayUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.PageDDayUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.RestoreDDayUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.UpdateDDayUseCase
import io.github.taetae98coding.diary.domain.tag.EntityTagKind
import io.github.taetae98coding.diary.domain.tag.usecase.AddEntityTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.GetEntityTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.RemoveEntityTagUseCase
import io.github.taetae98coding.diary.work.mcp.tool.McpTool
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArgumentException
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArguments
import io.github.taetae98coding.diary.work.mcp.tool.McpToolKind
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameter
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameterType
import io.github.taetae98coding.diary.work.mcp.tool.McpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.json.changedCountResult
import io.github.taetae98coding.diary.work.mcp.tool.json.colorOrNull
import io.github.taetae98coding.diary.work.mcp.tool.json.colorOrRandom
import io.github.taetae98coding.diary.work.mcp.tool.json.colorParameter
import io.github.taetae98coding.diary.work.mcp.tool.json.doneResult
import io.github.taetae98coding.diary.work.mcp.tool.json.idResult
import io.github.taetae98coding.diary.work.mcp.tool.json.toJson
import io.github.taetae98coding.diary.work.mcp.tool.json.toJsonArray
import io.github.taetae98coding.diary.work.mcp.tool.localDateOrNull
import io.github.taetae98coding.diary.work.mcp.tool.paging.mcpPageCursorParameter
import io.github.taetae98coding.diary.work.mcp.tool.paging.pageOffset
import io.github.taetae98coding.diary.work.mcp.tool.paging.readPage
import io.github.taetae98coding.diary.work.mcp.tool.paging.toJson
import io.github.taetae98coding.diary.work.mcp.tool.readFirst
import io.github.taetae98coding.diary.work.mcp.tool.uuid
import io.github.taetae98coding.diary.work.mcp.tool.uuidSet
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.buildJsonObject
import org.koin.core.annotation.Factory
import kotlin.time.Clock
import kotlin.uuid.Uuid

private const val BASE_DATE = "baseDate"
private const val CLEAR_END_DATE = "clearEndDate"
private const val COUNTS_BASE_DAY = "countsBaseDay"
private const val DDAY_ID = "ddayId"
private const val DESCRIPTION = "description"
private const val END_DATE = "endDate"
private const val TITLE = "title"

private val ddayIdParameter: McpToolParameter =
    McpToolParameter(name = DDAY_ID, type = McpToolParameterType.STRING, description = "Id of the D-Day.", isRequired = true)

@Factory
internal class DDayMcpToolSet(
    private val pageDDayUseCase: PageDDayUseCase,
    private val findDDayUseCase: FindDDayUseCase,
    private val getEntityTagUseCase: GetEntityTagUseCase,
    private val addDDayUseCase: AddDDayUseCase,
    private val updateDDayUseCase: UpdateDDayUseCase,
    private val deleteDDayUseCase: DeleteDDayUseCase,
    private val restoreDDayUseCase: RestoreDDayUseCase,
    private val addEntityTagUseCase: AddEntityTagUseCase,
    private val removeEntityTagUseCase: RemoveEntityTagUseCase,
    private val clock: Clock,
) : McpToolSet {
    override val toolList: List<McpTool> =
        listOf(
            McpTool(
                name = "dday_list",
                description =
                    "Lists the D-Days shown on the D-Day home list by title, with the days left or passed counted from today. " +
                        "The filters the user chose in the app are applied.",
                parameterList = listOf(mcpPageCursorParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                val today = today()

                pageDDayUseCase
                    .readPage(parameter = Unit, offset = arguments.pageOffset())
                    .toJson { dday -> dday.toJson(today = today) }
            },
            McpTool(
                name = "dday_get",
                description = "Gets one D-Day with its linked tags and the days left or passed counted from today. Returns null when the D-Day does not exist.",
                parameterList = listOf(ddayIdParameter),
                kind = McpToolKind.READ,
            ) { arguments -> getDDay(arguments) },
            McpTool(
                name = "dday_add",
                description =
                    "Adds a D-Day. The title must not be blank and the end date must not be before the base date. " +
                        "Omitted values use what the D-Day add screen fills in first: today as the base date, counting the base date as day 1, " +
                        "no repeat intervals, no end date, clamp_to_month_end and a random color.",
                parameterList =
                    listOf(
                        McpToolParameter(name = TITLE, type = McpToolParameterType.STRING, description = "Title of the D-Day.", isRequired = true),
                        McpToolParameter(name = DESCRIPTION, type = McpToolParameterType.STRING, description = "Description of the D-Day."),
                        colorParameter(description = "Color of the D-Day. A random color is used when omitted."),
                        McpToolParameter(name = BASE_DATE, type = McpToolParameterType.STRING, description = "Base date of the D-Day, like 2026-01-31. Today is used when omitted."),
                        McpToolParameter(name = COUNTS_BASE_DAY, type = McpToolParameterType.BOOLEAN, description = "Whether the base date counts as day 1. True when omitted."),
                        repeatsParameter(description = "Intervals of anniversaries after the base date."),
                        McpToolParameter(name = END_DATE, type = McpToolParameterType.STRING, description = "Last date anniversaries are shown, like 2026-12-31."),
                        overflowPolicyParameter,
                        McpToolParameter(name = "tagIds", type = McpToolParameterType.STRING_LIST, description = "Ids of tags to link."),
                    ),
                kind = McpToolKind.WRITE,
            ) { arguments -> addDDay(arguments) },
            McpTool(
                name = "dday_update",
                description =
                    "Updates a D-Day. Omitted fields keep their current value. A blank title keeps the current title. " +
                        "The end date must not be before the base date after the update. Tags are changed with dday_tag_add and dday_tag_remove.",
                parameterList =
                    listOf(
                        ddayIdParameter,
                        McpToolParameter(name = TITLE, type = McpToolParameterType.STRING, description = "New title."),
                        McpToolParameter(name = DESCRIPTION, type = McpToolParameterType.STRING, description = "New description."),
                        colorParameter(description = "New color."),
                        McpToolParameter(name = BASE_DATE, type = McpToolParameterType.STRING, description = "New base date, like 2026-01-31."),
                        McpToolParameter(name = COUNTS_BASE_DAY, type = McpToolParameterType.BOOLEAN, description = "Whether the base date counts as day 1."),
                        repeatsParameter(description = "New repeat intervals. They replace all current intervals; pass an empty list to remove them."),
                        McpToolParameter(name = END_DATE, type = McpToolParameterType.STRING, description = "New end date, like 2026-12-31."),
                        McpToolParameter(name = CLEAR_END_DATE, type = McpToolParameterType.BOOLEAN, description = "Set true to remove the end date. It wins over endDate."),
                        overflowPolicyParameter,
                    ),
                kind = McpToolKind.WRITE,
            ) { arguments -> updateDDay(arguments) },
            McpTool(
                name = "dday_delete",
                description = "Deletes a D-Day. Use dday_restore with the same id to undo.",
                parameterList = listOf(ddayIdParameter),
                kind = McpToolKind.DESTRUCTIVE,
            ) { arguments -> changedCountResult(deleteDDayUseCase(parameter = arguments.uuid(DDAY_ID)).getOrThrow()) },
            McpTool(
                name = "dday_restore",
                description = "Restores a deleted D-Day.",
                parameterList = listOf(ddayIdParameter),
                kind = McpToolKind.WRITE,
            ) { arguments -> changedCountResult(restoreDDayUseCase(parameter = arguments.uuid(DDAY_ID)).getOrThrow()) },
            tagLinkTool(name = "dday_tag_add", description = "Links a tag to a D-Day.") { ddayId, tagId ->
                addEntityTagUseCase(parameter = AddEntityTagUseCase.Parameter(kind = EntityTagKind.DDAY, entityId = ddayId, tagId = tagId)).getOrThrow()
            },
            tagLinkTool(name = "dday_tag_remove", description = "Unlinks a tag from a D-Day.") { ddayId, tagId ->
                removeEntityTagUseCase(parameter = RemoveEntityTagUseCase.Parameter(kind = EntityTagKind.DDAY, entityId = ddayId, tagId = tagId)).getOrThrow()
            },
        )

    private suspend fun getDDay(arguments: McpToolArguments): JsonElement {
        val ddayId = arguments.uuid(DDAY_ID)
        val dday = findDDayUseCase.readFirst(parameter = ddayId) ?: return JsonNull
        val tagList = getEntityTagUseCase.readFirst(parameter = GetEntityTagUseCase.Parameter(kind = EntityTagKind.DDAY, entityId = ddayId))

        return buildJsonObject {
            put("dday", dday.toJson(today = today()))
            put("tags", tagList.toJsonArray { tag -> tag.toJson() })
        }
    }

    private suspend fun addDDay(arguments: McpToolArguments): JsonElement {
        val input =
            DDayInput(
                title = arguments.string(TITLE),
                description = arguments.stringOrNull(DESCRIPTION).orEmpty(),
                color = arguments.colorOrRandom(),
                baseDate = arguments.localDateOrNull(BASE_DATE) ?: today(),
                countsBaseDay = arguments.booleanOrNull(COUNTS_BASE_DAY) ?: true,
                repeatList = arguments.repeatInputListOrNull().orEmpty(),
                endDate = arguments.localDateOrNull(END_DATE),
                overflowPolicy = arguments.overflowPolicyOrNull() ?: DDayOverflowPolicy.CLAMP_TO_MONTH_END,
            ).requireEndDateNotBeforeBaseDate()
        val id = addDDayUseCase(parameter = AddDDayUseCase.Parameter(input = input, tagIdSet = arguments.uuidSet("tagIds"))).getOrThrow()

        return idResult(id)
    }

    private suspend fun updateDDay(arguments: McpToolArguments): JsonElement {
        val ddayId = arguments.uuid(DDAY_ID)
        val newColor = arguments.colorOrNull()
        val newBaseDate = arguments.localDateOrNull(BASE_DATE)
        val newEndDate = arguments.localDateOrNull(END_DATE)
        val newRepeatList = arguments.repeatInputListOrNull()
        val newOverflowPolicy = arguments.overflowPolicyOrNull()
        val current = findDDayUseCase.readFirst(parameter = ddayId)?.detail ?: return changedCountResult(count = 0)
        val input =
            DDayInput(
                title = arguments.stringOrNull(TITLE) ?: current.title,
                description = arguments.stringOrNull(DESCRIPTION) ?: current.description,
                color = newColor ?: current.color,
                baseDate = newBaseDate ?: current.baseDate,
                countsBaseDay = arguments.booleanOrNull(COUNTS_BASE_DAY) ?: current.countsBaseDay,
                repeatList = newRepeatList ?: current.repeats.toRepeatInputList(),
                endDate = if (arguments.booleanOrNull(CLEAR_END_DATE) == true) null else newEndDate ?: current.endDate,
                overflowPolicy = newOverflowPolicy ?: current.overflowPolicy,
            ).requireEndDateNotBeforeBaseDate()

        return changedCountResult(updateDDayUseCase(parameter = UpdateDDayUseCase.Parameter(id = ddayId, input = input)).getOrThrow())
    }

    private fun today(): LocalDate = clock.todayIn(TimeZone.currentSystemDefault())

    private fun tagLinkTool(
        name: String,
        description: String,
        link: suspend (ddayId: Uuid, tagId: Uuid) -> Unit,
    ): McpTool =
        McpTool(
            name = name,
            description = description,
            parameterList =
                listOf(
                    ddayIdParameter,
                    McpToolParameter(name = "tagId", type = McpToolParameterType.STRING, description = "Id of the tag.", isRequired = true),
                ),
            kind = McpToolKind.WRITE,
        ) { arguments: McpToolArguments ->
            link(arguments.uuid(DDAY_ID), arguments.uuid("tagId"))
            doneResult as JsonElement
        }
}

private fun DDayInput.requireEndDateNotBeforeBaseDate(): DDayInput {
    val endDate = endDate ?: return this

    if (endDate < baseDate) throw McpToolArgumentException(message = "'$END_DATE' must not be before '$BASE_DATE'.")

    return this
}
