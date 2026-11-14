package io.github.taetae98coding.diary.core.database.api.routinerecord.datasource

import io.github.taetae98coding.diary.core.database.api.routinerecord.entity.RoutineRecordLocalEntity
import kotlin.uuid.Uuid

public interface AccountRoutineRecordSyncLocalDataSource {
    public suspend fun readPendingList(accountId: Uuid): List<RoutineRecordLocalEntity>
}
