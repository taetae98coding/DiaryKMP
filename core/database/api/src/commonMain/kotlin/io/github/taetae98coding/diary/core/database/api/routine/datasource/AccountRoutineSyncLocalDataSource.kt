package io.github.taetae98coding.diary.core.database.api.routine.datasource

import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineLocalEntity
import kotlin.uuid.Uuid

public interface AccountRoutineSyncLocalDataSource {
    public suspend fun readPendingList(accountId: Uuid): List<RoutineLocalEntity>
}
