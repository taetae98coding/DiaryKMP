package io.github.taetae98coding.diary.work.mcp.tool.routine

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import io.github.taetae98coding.diary.core.model.routine.Routine
import io.github.taetae98coding.diary.domain.routine.ROUTINE_AMOUNT_RANGE
import io.github.taetae98coding.diary.domain.routine.ROUTINE_DEFAULT_AMOUNT_PER_RECORD
import io.github.taetae98coding.diary.domain.routine.ROUTINE_DEFAULT_CYCLE
import io.github.taetae98coding.diary.domain.routine.ROUTINE_DEFAULT_UNIT
import io.github.taetae98coding.diary.domain.routine.ROUTINE_UNIT_MAX_LENGTH
import io.github.taetae98coding.diary.domain.routine.RoutineInput
import io.github.taetae98coding.diary.domain.routine.isValidRoutineUnit
import io.github.taetae98coding.diary.domain.routine.usecase.AddRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.FindRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.UpdateRoutineUseCase
import io.github.taetae98coding.diary.work.appaction.core.AppActionRandomColor
import io.github.taetae98coding.diary.work.appaction.core.readActive
import io.github.taetae98coding.diary.work.appaction.routine.RoutineAppAction
import io.github.taetae98coding.diary.work.appaction.routine.RoutineListAppAction
import io.github.taetae98coding.diary.work.appaction.routine.RoutineRecordAppAction
import io.github.taetae98coding.diary.work.appaction.tag.AppActionEntityTagKind
import io.github.taetae98coding.diary.work.appaction.tag.EntityTagAppAction
import io.github.taetae98coding.diary.work.mcp.tool.McpTool
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArgumentException
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArguments
import io.github.taetae98coding.diary.work.mcp.tool.McpToolKind
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameter
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameterType
import io.github.taetae98coding.diary.work.mcp.tool.McpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.appActionCall
import io.github.taetae98coding.diary.work.mcp.tool.cursorText
import io.github.taetae98coding.diary.work.mcp.tool.idText
import io.github.taetae98coding.diary.work.mcp.tool.json.changedCountResult
import io.github.taetae98coding.diary.work.mcp.tool.json.colorOrNull
import io.github.taetae98coding.diary.work.mcp.tool.json.colorParameter
import io.github.taetae98coding.diary.work.mcp.tool.json.idResult
import io.github.taetae98coding.diary.work.mcp.tool.json.toJson
import io.github.taetae98coding.diary.work.mcp.tool.localDateOrNull
import io.github.taetae98coding.diary.work.mcp.tool.localDateTextOrNull
import io.github.taetae98coding.diary.work.mcp.tool.paging.mcpPageCursorParameter
import io.github.taetae98coding.diary.work.mcp.tool.paging.toJson
import io.github.taetae98coding.diary.work.mcp.tool.tag.entityTagToolList
import io.github.taetae98coding.diary.work.mcp.tool.tag.tagIdsParameter
import io.github.taetae98coding.diary.work.mcp.tool.uuid
import io.github.taetae98coding.diary.work.mcp.tool.uuidSet
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlin.time.Clock

private const val AMOUNT = "amount"
private const val AMOUNT_PER_RECORD = "amountPerRecord"
private const val CLEAR_GOAL_AMOUNT = "clearGoalAmount"
private const val DATE = "date"
private const val DESCRIPTION = "description"
private const val GOAL_AMOUNT = "goalAmount"
private const val RECORD_ID = "recordId"
private const val ROUTINE_ID = "routineId"
private const val START_DATE = "startDate"
private const val TITLE = "title"
private val amountRangeText: String = "${ROUTINE_AMOUNT_RANGE.first} and ${ROUTINE_AMOUNT_RANGE.last}"

private val routineIdParameter: McpToolParameter =
    McpToolParameter(name = ROUTINE_ID, type = McpToolParameterType.STRING, description = "Id of the routine.", isRequired = true)

@Inject
@ContributesIntoSet(AppScope::class, binding = binding<McpToolSet>())
internal class RoutineMcpToolSet(
    private val routineListAppAction: RoutineListAppAction,
    private val routineAppAction: RoutineAppAction,
    private val routineRecordAppAction: RoutineRecordAppAction,
    private val entityTagAppAction: EntityTagAppAction,
    private val findRoutineUseCase: FindRoutineUseCase,
    private val addRoutineUseCase: AddRoutineUseCase,
    private val updateRoutineUseCase: UpdateRoutineUseCase,
    private val randomColor: AppActionRandomColor,
    private val clock: Clock,
) : McpToolSet {
    override val toolList: List<McpTool> =
        listOf(
            McpTool(
                name = "routine_list",
                description =
                    "Lists the routines shown on the routine home list, which are not finished, in progress first, then not started, by title within each, " +
                        "with this cycle's total and achievement rate counted from today. The tag filter the user chose in the app is applied. The view scope filter is not, so routines not started and routines not done on today's day of the week are also listed.",
                parameterList = listOf(mcpPageCursorParameter),
                kind = McpToolKind.READ,
            ) { arguments -> appActionCall { routineListAppAction.list(cursor = arguments.cursorText()) }.toJson { routine -> routine.toJson() } },
            McpTool(
                name = "routine_finished_list",
                description =
                    "Lists finished routines that are not deleted, in progress first, then not started, by title within each. " +
                        "The filters the user chose in the app are not applied.",
                parameterList = listOf(mcpPageCursorParameter),
                kind = McpToolKind.READ,
            ) { arguments -> appActionCall { routineListAppAction.finishedList(cursor = arguments.cursorText()) }.toJson { routine -> routine.toJson() } },
            McpTool(
                name = "routine_get",
                description =
                    "Gets one routine with its linked tags and this cycle's total and achievement rate counted from today. " +
                        "Returns null when the routine does not exist or is deleted.",
                parameterList = listOf(routineIdParameter),
                kind = McpToolKind.READ,
            ) { arguments -> appActionCall { routineAppAction.get(routineId = arguments.idText(ROUTINE_ID)) }?.toJson() ?: JsonNull },
            McpTool(
                name = "routine_add",
                description =
                    "Adds a routine with a random color. The title must not be blank and amounts must be between $amountRangeText. The routine is added as not finished. " +
                        "Omitted values use what the routine add screen fills in first: today, FREQ=DAILY, no unit, no goal and 1 per record.",
                parameterList =
                    listOf(
                        McpToolParameter(name = TITLE, type = McpToolParameterType.STRING, description = "Title of the routine.", isRequired = true),
                        McpToolParameter(name = DESCRIPTION, type = McpToolParameterType.STRING, description = "Description of the routine."),
                        McpToolParameter(name = START_DATE, type = McpToolParameterType.STRING, description = "First date records can be added, like 2026-01-31. Today is used when omitted."),
                        cycleParameter(description = "How often the routine repeats. FREQ=DAILY when omitted."),
                        unitParameter(description = "Unit of the amounts. The routine has no unit when omitted."),
                        McpToolParameter(name = GOAL_AMOUNT, type = McpToolParameterType.INTEGER, description = "Amount to reach in each cycle, between $amountRangeText. The routine has no goal when omitted."),
                        McpToolParameter(name = AMOUNT_PER_RECORD, type = McpToolParameterType.INTEGER, description = "Amount of one record when no amount is given, between $amountRangeText. 1 when omitted."),
                        tagIdsParameter,
                    ),
                kind = McpToolKind.WRITE,
            ) { arguments -> addRoutine(arguments) },
            McpTool(
                name = "routine_update",
                description =
                    "Updates a routine. Omitted fields keep their current value. A blank title keeps the current title. " +
                        "After the update amounts must be between $amountRangeText. " +
                        "Tags are changed with routine_tag_add and routine_tag_remove, and whether the routine is finished with routine_finish and routine_restart.",
                parameterList =
                    listOf(
                        routineIdParameter,
                        McpToolParameter(name = TITLE, type = McpToolParameterType.STRING, description = "New title."),
                        McpToolParameter(name = DESCRIPTION, type = McpToolParameterType.STRING, description = "New description."),
                        colorParameter(description = "New color."),
                        McpToolParameter(name = START_DATE, type = McpToolParameterType.STRING, description = "New start date, like 2026-01-31."),
                        cycleParameter(description = "New cycle."),
                        unitParameter(description = "New unit. Amounts already recorded are kept and read in the new unit. An empty string removes the unit."),
                        McpToolParameter(name = GOAL_AMOUNT, type = McpToolParameterType.INTEGER, description = "New goal amount, between $amountRangeText."),
                        McpToolParameter(name = CLEAR_GOAL_AMOUNT, type = McpToolParameterType.BOOLEAN, description = "Set true to remove the goal amount. It wins over goalAmount."),
                        McpToolParameter(name = AMOUNT_PER_RECORD, type = McpToolParameterType.INTEGER, description = "New amount of one record when no amount is given, between $amountRangeText."),
                    ),
                kind = McpToolKind.WRITE,
            ) { arguments -> updateRoutine(arguments) },
            McpTool(
                name = "routine_finish",
                description = "Marks a routine as finished. Its records are kept and records can still be added and removed.",
                parameterList = listOf(routineIdParameter),
                kind = McpToolKind.WRITE,
            ) { arguments -> appActionCall { routineAppAction.finish(routineId = arguments.idText(ROUTINE_ID)) }.toJson() },
            McpTool(
                name = "routine_restart",
                description = "Marks a finished routine as not finished.",
                parameterList = listOf(routineIdParameter),
                kind = McpToolKind.WRITE,
            ) { arguments -> appActionCall { routineAppAction.restart(routineId = arguments.idText(ROUTINE_ID)) }.toJson() },
            McpTool(
                name = "routine_delete",
                description = "Deletes a routine. Its records are kept. Use routine_restore with the same id to bring it back with its records.",
                parameterList = listOf(routineIdParameter),
                kind = McpToolKind.DESTRUCTIVE,
            ) { arguments -> appActionCall { routineAppAction.delete(routineId = arguments.idText(ROUTINE_ID)) }.toJson() },
            McpTool(
                name = "routine_restore",
                description = "Restores a deleted routine with its records.",
                parameterList = listOf(routineIdParameter),
                kind = McpToolKind.WRITE,
            ) { arguments -> appActionCall { routineAppAction.restore(routineId = arguments.idText(ROUTINE_ID)) }.toJson() },
            McpTool(
                name = "routine_record_list",
                description =
                    "Lists the records of a routine that are not removed, latest date first and, within a date, latest added first. " +
                        "Amounts are in the unit of the routine. Returns no records when the routine does not exist or is deleted.",
                parameterList = listOf(routineIdParameter, mcpPageCursorParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                appActionCall { routineRecordAppAction.list(routineId = arguments.idText(ROUTINE_ID), cursor = arguments.cursorText()) }.toJson { record -> record.toJson() }
            },
            McpTool(
                name = "routine_record_add",
                description =
                    "Adds a record to a routine. The date must be on or after the start date of the routine and the amount must be between $amountRangeText. " +
                        "Returns changedCount 0 and adds nothing when the routine does not exist or is deleted.",
                parameterList =
                    listOf(
                        routineIdParameter,
                        McpToolParameter(name = DATE, type = McpToolParameterType.STRING, description = "Date of the record, like 2026-01-31. Today is used when omitted."),
                        McpToolParameter(name = AMOUNT, type = McpToolParameterType.INTEGER, description = "Amount of the record in the unit of the routine. The routine's amount per record is used when omitted."),
                    ),
                kind = McpToolKind.WRITE,
            ) { arguments ->
                appActionCall {
                    routineRecordAppAction.add(routineId = arguments.idText(ROUTINE_ID), date = arguments.localDateTextOrNull(DATE), amount = arguments.intOrNull(AMOUNT))
                }.toJson()
            },
            McpTool(
                name = "routine_record_remove",
                description = "Removes a record from its routine. This cannot be undone; add it again with routine_record_add with the same date and amount if needed.",
                parameterList = listOf(McpToolParameter(name = RECORD_ID, type = McpToolParameterType.STRING, description = "Id of the routine record.", isRequired = true)),
                kind = McpToolKind.DESTRUCTIVE,
            ) { arguments -> appActionCall { routineRecordAppAction.remove(recordId = arguments.idText(RECORD_ID)) }.toJson() },
        ) +
            entityTagToolList(
                toolNamePrefix = "routine",
                entityName = "a routine",
                kind = AppActionEntityTagKind.ROUTINE,
                entityIdParameter = routineIdParameter,
                entityTagAppAction = entityTagAppAction,
            )

    private suspend fun addRoutine(arguments: McpToolArguments): JsonElement {
        val input =
            RoutineInput(
                title = arguments.string(TITLE),
                description = arguments.stringOrNull(DESCRIPTION).orEmpty(),
                color = randomColor.next(),
                startDate = arguments.localDateOrNull(START_DATE) ?: today(),
                cycle = arguments.cycleOrNull() ?: ROUTINE_DEFAULT_CYCLE,
                unit = arguments.stringOrNull(UNIT) ?: ROUTINE_DEFAULT_UNIT,
                goalAmount = arguments.intOrNull(GOAL_AMOUNT),
                amountPerRecord = arguments.intOrNull(AMOUNT_PER_RECORD) ?: ROUTINE_DEFAULT_AMOUNT_PER_RECORD,
            ).requireValid()
        val id = addRoutineUseCase(parameter = AddRoutineUseCase.Parameter(input = input, tagIdSet = arguments.uuidSet("tagIds"))).getOrThrow()

        return idResult(id)
    }

    private suspend fun updateRoutine(arguments: McpToolArguments): JsonElement {
        val routineId = arguments.uuid(ROUTINE_ID)
        val newColor = arguments.colorOrNull()
        val newStartDate = arguments.localDateOrNull(START_DATE)
        val newCycle = arguments.cycleOrNull()
        val newUnit = arguments.stringOrNull(UNIT)
        val newGoalAmount = arguments.intOrNull(GOAL_AMOUNT)
        val newAmountPerRecord = arguments.intOrNull(AMOUNT_PER_RECORD)
        val current = findRoutineUseCase.readActive(id = routineId, isDeleted = Routine::isDeleted)?.detail ?: return changedCountResult(count = 0)
        val input =
            RoutineInput(
                title = arguments.stringOrNull(TITLE) ?: current.title,
                description = arguments.stringOrNull(DESCRIPTION) ?: current.description,
                color = newColor ?: current.color,
                startDate = newStartDate ?: current.startDate,
                cycle = newCycle ?: current.cycle,
                unit = newUnit ?: current.unit,
                goalAmount = if (arguments.booleanOrNull(CLEAR_GOAL_AMOUNT) == true) null else newGoalAmount ?: current.goalAmount,
                amountPerRecord = newAmountPerRecord ?: current.amountPerRecord,
            ).requireValid()

        return changedCountResult(updateRoutineUseCase(parameter = UpdateRoutineUseCase.Parameter(id = routineId, input = input)).getOrThrow())
    }

    private fun today(): LocalDate = clock.todayIn(TimeZone.currentSystemDefault())
}

private fun RoutineInput.requireValid(): RoutineInput {
    val goalAmount = goalAmount
    val message =
        when {
            goalAmount != null && goalAmount !in ROUTINE_AMOUNT_RANGE -> "'$GOAL_AMOUNT' must be between $amountRangeText."
            amountPerRecord !in ROUTINE_AMOUNT_RANGE -> "'$AMOUNT_PER_RECORD' must be between $amountRangeText."
            !unit.trim().isValidRoutineUnit() -> "'$UNIT' must be at most $ROUTINE_UNIT_MAX_LENGTH characters without line breaks."
            else -> return this
        }

    throw McpToolArgumentException(message = message)
}
