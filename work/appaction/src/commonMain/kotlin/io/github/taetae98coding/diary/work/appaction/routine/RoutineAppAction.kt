package io.github.taetae98coding.diary.work.appaction.routine

import io.github.taetae98coding.diary.core.model.routine.Routine
import io.github.taetae98coding.diary.domain.routine.ROUTINE_AMOUNT_RANGE
import io.github.taetae98coding.diary.domain.routine.ROUTINE_DEFAULT_AMOUNT_PER_RECORD
import io.github.taetae98coding.diary.domain.routine.ROUTINE_DEFAULT_CYCLE
import io.github.taetae98coding.diary.domain.routine.ROUTINE_DEFAULT_UNIT
import io.github.taetae98coding.diary.domain.routine.ROUTINE_UNIT_MAX_LENGTH
import io.github.taetae98coding.diary.domain.routine.RoutineInput
import io.github.taetae98coding.diary.domain.routine.isValidRoutineUnit
import io.github.taetae98coding.diary.domain.routine.usecase.AddRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.DeleteRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.FindRoutineSummaryUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.FindRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.FinishRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.RestartRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.RestoreRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.UpdateRoutineUseCase
import io.github.taetae98coding.diary.domain.tag.EntityTagKind
import io.github.taetae98coding.diary.domain.tag.usecase.GetEntityTagUseCase
import io.github.taetae98coding.diary.work.appaction.core.AppActionChangedCount
import io.github.taetae98coding.diary.work.appaction.core.AppActionId
import io.github.taetae98coding.diary.work.appaction.core.AppActionRandomColor
import io.github.taetae98coding.diary.work.appaction.core.changeActive
import io.github.taetae98coding.diary.work.appaction.core.invalidArgument
import io.github.taetae98coding.diary.work.appaction.core.readActive
import io.github.taetae98coding.diary.work.appaction.core.readFirst
import io.github.taetae98coding.diary.work.appaction.core.runAppAction
import io.github.taetae98coding.diary.work.appaction.core.toAppActionChangedCount
import io.github.taetae98coding.diary.work.appaction.core.toAppActionId
import io.github.taetae98coding.diary.work.appaction.core.toColorArgumentOrNull
import io.github.taetae98coding.diary.work.appaction.core.toLocalDateArgumentOrNull
import io.github.taetae98coding.diary.work.appaction.core.toUuidArgument
import io.github.taetae98coding.diary.work.appaction.core.toUuidSetArgument
import io.github.taetae98coding.diary.work.appaction.tag.toAppActionTag
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.koin.core.annotation.Factory
import kotlin.time.Clock

internal const val ROUTINE_ID: String = "routineId"
private const val AMOUNT_PER_RECORD = "amountPerRecord"
private const val GOAL_AMOUNT = "goalAmount"
private const val START_DATE = "startDate"
private const val UNIT = "unit"

@Factory
public class RoutineAppAction internal constructor(
    private val findRoutineUseCase: FindRoutineUseCase,
    private val findRoutineSummaryUseCase: FindRoutineSummaryUseCase,
    private val getEntityTagUseCase: GetEntityTagUseCase,
    private val addRoutineUseCase: AddRoutineUseCase,
    private val updateRoutineUseCase: UpdateRoutineUseCase,
    private val finishRoutineUseCase: FinishRoutineUseCase,
    private val restartRoutineUseCase: RestartRoutineUseCase,
    private val deleteRoutineUseCase: DeleteRoutineUseCase,
    private val restoreRoutineUseCase: RestoreRoutineUseCase,
    private val randomColor: AppActionRandomColor,
    private val clock: Clock,
) {
    public suspend fun get(routineId: String): AppActionRoutineDetail? = runAppAction { detail(routineId = routineId, includesDeleted = false) }

    public suspend fun getIncludingDeleted(routineId: String): AppActionRoutineDetail? = runAppAction { detail(routineId = routineId, includesDeleted = true) }

    private suspend fun detail(
        routineId: String,
        includesDeleted: Boolean,
    ): AppActionRoutineDetail? {
        val id = routineId.toUuidArgument(name = ROUTINE_ID)
        val today = today()
        val summary =
            findRoutineSummaryUseCase
                .readFirst(parameter = FindRoutineSummaryUseCase.Parameter(routineId = id, today = today))
                ?.takeIf { summary -> includesDeleted || !summary.routine.isDeleted }
                ?: return null

        return AppActionRoutineDetail(
            routine = summary.toAppActionRoutine(today = today),
            tags = getEntityTagUseCase.readFirst(parameter = GetEntityTagUseCase.Parameter(kind = EntityTagKind.ROUTINE, entityId = id)).map { tag -> tag.toAppActionTag() },
        )
    }

    public suspend fun add(
        title: String,
        description: String?,
        startDate: String?,
        cycle: String?,
        unit: String?,
        goalAmount: Int?,
        amountPerRecord: Int?,
        tagIds: List<String>,
    ): AppActionId =
        runAppAction {
            val input =
                RoutineInput(
                    title = title,
                    description = description.orEmpty(),
                    color = randomColor.next(),
                    startDate = startDate.toLocalDateArgumentOrNull(name = START_DATE) ?: today(),
                    cycle = cycle.toRoutineCycleArgumentOrNull() ?: ROUTINE_DEFAULT_CYCLE,
                    unit = unit ?: ROUTINE_DEFAULT_UNIT,
                    goalAmount = goalAmount,
                    amountPerRecord = amountPerRecord ?: ROUTINE_DEFAULT_AMOUNT_PER_RECORD,
                ).requireValid()
            val parameter = AddRoutineUseCase.Parameter(input = input, tagIdSet = tagIds.toUuidSetArgument(name = "tagIds"))

            addRoutineUseCase(parameter = parameter).getOrThrow().toAppActionId()
        }

    // 목표량은 담지 않음과 지움을 구분해야 하므로 clearGoalAmount로 지움을 받는다.
    public suspend fun update(
        routineId: String,
        title: String?,
        description: String?,
        color: String?,
        startDate: String?,
        cycle: String?,
        unit: String?,
        goalAmount: Int?,
        clearGoalAmount: Boolean?,
        amountPerRecord: Int?,
    ): AppActionChangedCount =
        runAppAction {
            val id = routineId.toUuidArgument(name = ROUTINE_ID)
            val newColor = color.toColorArgumentOrNull()
            val newStartDate = startDate.toLocalDateArgumentOrNull(name = START_DATE)
            val newCycle = cycle.toRoutineCycleArgumentOrNull()
            val current = findRoutineUseCase.readActive(id = id, isDeleted = Routine::isDeleted)?.detail ?: return@runAppAction 0.toAppActionChangedCount()
            val input =
                RoutineInput(
                    title = title ?: current.title,
                    description = description ?: current.description,
                    color = newColor ?: current.color,
                    startDate = newStartDate ?: current.startDate,
                    cycle = newCycle ?: current.cycle,
                    unit = unit ?: current.unit,
                    goalAmount = if (clearGoalAmount == true) null else goalAmount ?: current.goalAmount,
                    amountPerRecord = amountPerRecord ?: current.amountPerRecord,
                ).requireValid()

            updateRoutineUseCase(parameter = UpdateRoutineUseCase.Parameter(id = id, input = input)).getOrThrow().toAppActionChangedCount()
        }

    public suspend fun finish(routineId: String): AppActionChangedCount =
        runAppAction {
            val id = routineId.toUuidArgument(name = ROUTINE_ID)

            findRoutineUseCase.changeActive(id = id, isDeleted = Routine::isDeleted) { finishRoutineUseCase(parameter = id) }
        }

    public suspend fun restart(routineId: String): AppActionChangedCount =
        runAppAction {
            val id = routineId.toUuidArgument(name = ROUTINE_ID)

            findRoutineUseCase.changeActive(id = id, isDeleted = Routine::isDeleted) { restartRoutineUseCase(parameter = id) }
        }

    public suspend fun delete(routineId: String): AppActionChangedCount =
        runAppAction {
            val id = routineId.toUuidArgument(name = ROUTINE_ID)

            findRoutineUseCase.changeActive(id = id, isDeleted = Routine::isDeleted) { deleteRoutineUseCase(parameter = id) }
        }

    public suspend fun restore(routineId: String): AppActionChangedCount = runAppAction { restoreRoutineUseCase(parameter = routineId.toUuidArgument(name = ROUTINE_ID)).getOrThrow().toAppActionChangedCount() }

    private fun today(): LocalDate = clock.todayIn(TimeZone.currentSystemDefault())
}

private fun RoutineInput.requireValid(): RoutineInput {
    val goalAmount = goalAmount

    if (goalAmount != null && goalAmount !in ROUTINE_AMOUNT_RANGE) invalidArgument(message = "'$GOAL_AMOUNT' must be between ${ROUTINE_AMOUNT_RANGE.first} and ${ROUTINE_AMOUNT_RANGE.last}.")
    if (amountPerRecord !in ROUTINE_AMOUNT_RANGE) invalidArgument(message = "'$AMOUNT_PER_RECORD' must be between ${ROUTINE_AMOUNT_RANGE.first} and ${ROUTINE_AMOUNT_RANGE.last}.")
    if (!unit.trim().isValidRoutineUnit()) invalidArgument(message = "'$UNIT' must be at most $ROUTINE_UNIT_MAX_LENGTH characters without line breaks.")

    return this
}
