package io.github.taetae98coding.diary.work.appaction.appfunction.routine

import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionChangedCount
import io.github.taetae98coding.diary.work.appaction.appfunction.core.appFunctionCall
import io.github.taetae98coding.diary.work.appaction.appfunction.core.toAppFunctionChangedCount
import io.github.taetae98coding.diary.work.appaction.routine.RoutineRecordAppAction

internal class RoutineRecordAppFunction(
    private val routineRecordAppAction: RoutineRecordAppAction,
) {
    suspend fun list(
        routineId: String,
        cursor: String?,
    ): AppFunctionRoutineRecordPage = appFunctionCall { routineRecordAppAction.list(routineId = routineId, cursor = cursor).toAppFunctionRoutineRecordPage() }

    suspend fun add(
        routineId: String,
        date: String?,
        amount: Int?,
    ): AppFunctionChangedCount = appFunctionCall { routineRecordAppAction.add(routineId = routineId, date = date, amount = amount).toAppFunctionChangedCount() }

    suspend fun remove(recordId: String): AppFunctionChangedCount = appFunctionCall { routineRecordAppAction.remove(recordId = recordId).toAppFunctionChangedCount() }
}
