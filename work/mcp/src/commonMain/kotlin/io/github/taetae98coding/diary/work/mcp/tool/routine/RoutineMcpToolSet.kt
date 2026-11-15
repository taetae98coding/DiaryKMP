package io.github.taetae98coding.diary.work.mcp.tool.routine

import io.github.taetae98coding.diary.core.model.routine.Routine
import io.github.taetae98coding.diary.core.model.routine.RoutineRecord
import io.github.taetae98coding.diary.domain.routine.ROUTINE_AMOUNT_RANGE
import io.github.taetae98coding.diary.domain.routine.ROUTINE_DEFAULT_AMOUNT_PER_RECORD
import io.github.taetae98coding.diary.domain.routine.ROUTINE_DEFAULT_CYCLE
import io.github.taetae98coding.diary.domain.routine.ROUTINE_DEFAULT_UNIT
import io.github.taetae98coding.diary.domain.routine.ROUTINE_UNIT_MAX_LENGTH
import io.github.taetae98coding.diary.domain.routine.RoutineInput
import io.github.taetae98coding.diary.domain.routine.isValidRoutineUnit
import io.github.taetae98coding.diary.domain.routine.progress.canRecord
import io.github.taetae98coding.diary.domain.routine.usecase.AddRoutineRecordUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.AddRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.DeleteRoutineRecordUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.DeleteRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.FindRoutineRecordUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.FindRoutineSummaryUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.FindRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.FinishRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.PageFinishedRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.PageRoutineRecordUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.PageRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.RestartRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.RestoreRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.UpdateRoutineUseCase
import io.github.taetae98coding.diary.domain.tag.EntityTagKind
import io.github.taetae98coding.diary.domain.tag.usecase.AddEntityTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.FindTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.GetEntityTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.RemoveEntityTagUseCase
import io.github.taetae98coding.diary.work.appaction.core.AppActionRandomColor
import io.github.taetae98coding.diary.work.mcp.tool.McpTool
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArgumentException
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArguments
import io.github.taetae98coding.diary.work.mcp.tool.McpToolKind
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameter
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameterType
import io.github.taetae98coding.diary.work.mcp.tool.McpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.changeActive
import io.github.taetae98coding.diary.work.mcp.tool.isActive
import io.github.taetae98coding.diary.work.mcp.tool.json.changedCountResult
import io.github.taetae98coding.diary.work.mcp.tool.json.colorOrNull
import io.github.taetae98coding.diary.work.mcp.tool.json.colorParameter
import io.github.taetae98coding.diary.work.mcp.tool.json.idResult
import io.github.taetae98coding.diary.work.mcp.tool.json.toJson
import io.github.taetae98coding.diary.work.mcp.tool.json.toJsonArray
import io.github.taetae98coding.diary.work.mcp.tool.localDateOrNull
import io.github.taetae98coding.diary.work.mcp.tool.pageActive
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
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.buildJsonObject
import org.koin.core.annotation.Factory
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

@Factory
internal class RoutineMcpToolSet(
    private val pageRoutineUseCase: PageRoutineUseCase,
    private val pageFinishedRoutineUseCase: PageFinishedRoutineUseCase,
    private val findRoutineUseCase: FindRoutineUseCase,
    private val findRoutineSummaryUseCase: FindRoutineSummaryUseCase,
    private val getEntityTagUseCase: GetEntityTagUseCase,
    private val addRoutineUseCase: AddRoutineUseCase,
    private val updateRoutineUseCase: UpdateRoutineUseCase,
    private val finishRoutineUseCase: FinishRoutineUseCase,
    private val restartRoutineUseCase: RestartRoutineUseCase,
    private val deleteRoutineUseCase: DeleteRoutineUseCase,
    private val restoreRoutineUseCase: RestoreRoutineUseCase,
    private val findRoutineRecordUseCase: FindRoutineRecordUseCase,
    private val pageRoutineRecordUseCase: PageRoutineRecordUseCase,
    private val addRoutineRecordUseCase: AddRoutineRecordUseCase,
    private val deleteRoutineRecordUseCase: DeleteRoutineRecordUseCase,
    private val findTagUseCase: FindTagUseCase,
    private val addEntityTagUseCase: AddEntityTagUseCase,
    private val removeEntityTagUseCase: RemoveEntityTagUseCase,
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
            ) { arguments ->
                val today = today()

                pageRoutineUseCase
                    .readPage(parameter = today, offset = arguments.pageOffset())
                    .toJson { summary -> summary.toJson(today = today) }
            },
            McpTool(
                name = "routine_finished_list",
                description =
                    "Lists finished routines that are not deleted, in progress first, then not started, by title within each. " +
                        "The filters the user chose in the app are not applied.",
                parameterList = listOf(mcpPageCursorParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                val today = today()

                pageFinishedRoutineUseCase
                    .readPage(parameter = today, offset = arguments.pageOffset())
                    .toJson { summary -> summary.toJson(today = today) }
            },
            McpTool(
                name = "routine_get",
                description =
                    "Gets one routine with its linked tags and this cycle's total and achievement rate counted from today. " +
                        "Returns null when the routine does not exist or is deleted.",
                parameterList = listOf(routineIdParameter),
                kind = McpToolKind.READ,
            ) { arguments -> getRoutine(arguments) },
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
            ) { arguments -> findRoutineUseCase.changeActive(id = arguments.uuid(ROUTINE_ID), isDeleted = Routine::isDeleted) { id -> finishRoutineUseCase(parameter = id) } },
            McpTool(
                name = "routine_restart",
                description = "Marks a finished routine as not finished.",
                parameterList = listOf(routineIdParameter),
                kind = McpToolKind.WRITE,
            ) { arguments -> findRoutineUseCase.changeActive(id = arguments.uuid(ROUTINE_ID), isDeleted = Routine::isDeleted) { id -> restartRoutineUseCase(parameter = id) } },
            McpTool(
                name = "routine_delete",
                description = "Deletes a routine. Its records are kept. Use routine_restore with the same id to bring it back with its records.",
                parameterList = listOf(routineIdParameter),
                kind = McpToolKind.DESTRUCTIVE,
            ) { arguments -> findRoutineUseCase.changeActive(id = arguments.uuid(ROUTINE_ID), isDeleted = Routine::isDeleted) { id -> deleteRoutineUseCase(parameter = id) } },
            McpTool(
                name = "routine_restore",
                description = "Restores a deleted routine with its records.",
                parameterList = listOf(routineIdParameter),
                kind = McpToolKind.WRITE,
            ) { arguments -> changedCountResult(restoreRoutineUseCase(parameter = arguments.uuid(ROUTINE_ID)).getOrThrow()) },
            McpTool(
                name = "routine_record_list",
                description =
                    "Lists the records of a routine that are not removed, latest date first and, within a date, latest added first. " +
                        "Amounts are in the unit of the routine. Returns no records when the routine does not exist or is deleted.",
                parameterList = listOf(routineIdParameter, mcpPageCursorParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                val routineId = arguments.uuid(ROUTINE_ID)
                val offset = arguments.pageOffset()

                findRoutineUseCase.pageActive(id = routineId, isDeleted = Routine::isDeleted) {
                    pageRoutineRecordUseCase.readPage(parameter = routineId, offset = offset).toJson { record -> record.toJson() }
                }
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
            ) { arguments -> addRecord(arguments) },
            McpTool(
                name = "routine_record_remove",
                description = "Removes a record from its routine. This cannot be undone; add it again with routine_record_add with the same date and amount if needed.",
                parameterList = listOf(McpToolParameter(name = RECORD_ID, type = McpToolParameterType.STRING, description = "Id of the routine record.", isRequired = true)),
                kind = McpToolKind.DESTRUCTIVE,
            ) { arguments -> removeRecord(arguments) },
        ) +
            entityTagToolList(
                toolNamePrefix = "routine",
                entityName = "a routine",
                kind = EntityTagKind.ROUTINE,
                entityIdParameter = routineIdParameter,
                findTagUseCase = findTagUseCase,
                addEntityTagUseCase = addEntityTagUseCase,
                removeEntityTagUseCase = removeEntityTagUseCase,
            ) { id -> findRoutineUseCase.isActive(id = id, isDeleted = Routine::isDeleted) }

    private suspend fun getRoutine(arguments: McpToolArguments): JsonElement {
        val routineId = arguments.uuid(ROUTINE_ID)
        val today = today()
        val summary =
            findRoutineSummaryUseCase
                .readFirst(parameter = FindRoutineSummaryUseCase.Parameter(routineId = routineId, today = today))
                ?.takeUnless { summary -> summary.routine.isDeleted }
                ?: return JsonNull
        val tagList = getEntityTagUseCase.readFirst(parameter = GetEntityTagUseCase.Parameter(kind = EntityTagKind.ROUTINE, entityId = routineId))

        return buildJsonObject {
            put("routine", summary.toJson(today = today))
            put("tags", tagList.toJsonArray { tag -> tag.toJson() })
        }
    }

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

    private suspend fun addRecord(arguments: McpToolArguments): JsonElement {
        val routineId = arguments.uuid(ROUTINE_ID)
        val newDate = arguments.localDateOrNull(DATE)
        val newAmount = arguments.intOrNull(AMOUNT)
        val detail = findRoutineUseCase.readActive(id = routineId, isDeleted = Routine::isDeleted)?.detail ?: return changedCountResult(count = 0)
        val parameter =
            AddRoutineRecordUseCase.Parameter(
                routineId = routineId,
                date = newDate ?: today(),
                amount = newAmount ?: detail.amountPerRecord,
            )

        if (parameter.amount !in ROUTINE_AMOUNT_RANGE) throw McpToolArgumentException(message = "'$AMOUNT' must be between $amountRangeText.")
        if (!detail.canRecord(date = parameter.date)) throw McpToolArgumentException(message = "'$DATE' must be on or after the start date of the routine.")

        addRoutineRecordUseCase(parameter = parameter).getOrThrow()

        return changedCountResult(count = 1)
    }

    private suspend fun removeRecord(arguments: McpToolArguments): JsonElement {
        val recordId = arguments.uuid(RECORD_ID)
        val record = findRoutineRecordUseCase.readActive(id = recordId, isDeleted = RoutineRecord::isDeleted) ?: return changedCountResult(count = 0)

        return findRoutineUseCase.changeActive(id = record.routineId, isDeleted = Routine::isDeleted) { deleteRoutineRecordUseCase(parameter = recordId) }
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
