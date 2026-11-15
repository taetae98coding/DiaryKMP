package io.github.taetae98coding.diary.core.database.api.routine.transaction

import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineLocalEntity
import kotlin.time.Instant
import kotlin.uuid.Uuid

public interface AccountRoutineTransaction {
    public suspend fun upsert(
        accountId: Uuid,
        routine: RoutineLocalEntity,
        routineTagList: List<EntityTagLocalEntity>,
    )

    public suspend fun updateDetail(
        accountId: Uuid,
        routineId: Uuid,
        detail: RoutineDetailLocalEntity,
        updatedAt: Instant,
    ): Int

    public suspend fun updateFinished(
        accountId: Uuid,
        routineId: Uuid,
        isFinished: Boolean,
        updatedAt: Instant,
    ): Int

    public suspend fun updateDeleted(
        accountId: Uuid,
        routineId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int
}
