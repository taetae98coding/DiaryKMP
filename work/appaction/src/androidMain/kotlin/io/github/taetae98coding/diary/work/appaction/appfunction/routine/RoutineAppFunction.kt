package io.github.taetae98coding.diary.work.appaction.appfunction.routine

import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionChangedCount
import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionId
import io.github.taetae98coding.diary.work.appaction.appfunction.core.appFunctionCall
import io.github.taetae98coding.diary.work.appaction.appfunction.core.toAppFunctionChangedCount
import io.github.taetae98coding.diary.work.appaction.appfunction.core.toAppFunctionId
import io.github.taetae98coding.diary.work.appaction.routine.RoutineAppAction
import io.github.taetae98coding.diary.work.appaction.routine.RoutineListAppAction

internal class RoutineAppFunction(
    private val routineAppAction: RoutineAppAction,
    private val routineListAppAction: RoutineListAppAction,
) {
    suspend fun list(cursor: String?): AppFunctionRoutinePage = appFunctionCall { routineListAppAction.list(cursor = cursor).toAppFunctionRoutinePage() }

    suspend fun finishedList(cursor: String?): AppFunctionRoutinePage = appFunctionCall { routineListAppAction.finishedList(cursor = cursor).toAppFunctionRoutinePage() }

    suspend fun get(routineId: String): AppFunctionRoutineDetail? = appFunctionCall { routineAppAction.get(routineId = routineId)?.toAppFunctionRoutineDetail() }

    suspend fun add(
        title: String,
        description: String?,
        startDate: String?,
        cycle: String?,
        unit: String?,
        goalAmount: Int?,
        amountPerRecord: Int?,
        tagIds: List<String>,
    ): AppFunctionId =
        appFunctionCall {
            routineAppAction
                .add(
                    title = title,
                    description = description,
                    startDate = startDate,
                    cycle = cycle,
                    unit = unit,
                    goalAmount = goalAmount,
                    amountPerRecord = amountPerRecord,
                    tagIds = tagIds,
                ).toAppFunctionId()
        }

    suspend fun update(
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
    ): AppFunctionChangedCount =
        appFunctionCall {
            routineAppAction
                .update(
                    routineId = routineId,
                    title = title,
                    description = description,
                    color = color,
                    startDate = startDate,
                    cycle = cycle,
                    unit = unit,
                    goalAmount = goalAmount,
                    clearGoalAmount = clearGoalAmount,
                    amountPerRecord = amountPerRecord,
                ).toAppFunctionChangedCount()
        }

    suspend fun finish(routineId: String): AppFunctionChangedCount = appFunctionCall { routineAppAction.finish(routineId = routineId).toAppFunctionChangedCount() }

    suspend fun restart(routineId: String): AppFunctionChangedCount = appFunctionCall { routineAppAction.restart(routineId = routineId).toAppFunctionChangedCount() }

    suspend fun delete(routineId: String): AppFunctionChangedCount = appFunctionCall { routineAppAction.delete(routineId = routineId).toAppFunctionChangedCount() }

    suspend fun restore(routineId: String): AppFunctionChangedCount = appFunctionCall { routineAppAction.restore(routineId = routineId).toAppFunctionChangedCount() }
}
