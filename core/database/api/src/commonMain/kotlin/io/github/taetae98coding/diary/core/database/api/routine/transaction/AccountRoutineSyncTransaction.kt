package io.github.taetae98coding.diary.core.database.api.routine.transaction

import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineLocalEntity
import kotlin.uuid.Uuid

public interface AccountRoutineSyncTransaction {
    public suspend fun clearPending(
        accountId: Uuid,
        routineList: List<RoutineLocalEntity>,
    )

    public suspend fun upsert(
        accountId: Uuid,
        routineList: List<RoutineLocalEntity>,
        cursor: Long,
    )
}
