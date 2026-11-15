package io.github.taetae98coding.diary.core.database.api.routinerecord.transaction

import io.github.taetae98coding.diary.core.database.api.routinerecord.entity.RoutineRecordLocalEntity
import kotlin.time.Instant
import kotlin.uuid.Uuid

public interface AccountRoutineRecordTransaction {
    public suspend fun upsert(
        accountId: Uuid,
        record: RoutineRecordLocalEntity,
    ): Int

    public suspend fun updateDeleted(
        accountId: Uuid,
        recordId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int
}
