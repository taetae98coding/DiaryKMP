package io.github.taetae98coding.diary.core.database.api.routinerecord.transaction

import io.github.taetae98coding.diary.core.database.api.routinerecord.entity.RoutineRecordLocalEntity
import kotlin.uuid.Uuid

public interface AccountRoutineRecordSyncTransaction {
    public suspend fun clearPending(
        accountId: Uuid,
        recordList: List<RoutineRecordLocalEntity>,
    )

    public suspend fun upsert(
        accountId: Uuid,
        recordList: List<RoutineRecordLocalEntity>,
        cursor: Long,
    )
}
