package io.github.taetae98coding.diary.core.database.impl.routine.dao

import androidx.room3.Dao
import androidx.room3.Query
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineLocalEntity
import io.github.taetae98coding.diary.core.database.impl.routine.entity.AccountRoutineLocalEntity
import io.github.taetae98coding.diary.library.room3.dao.RoomDao
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
internal interface AccountRoutineSyncDao : RoomDao<AccountRoutineLocalEntity> {
    @Query(
        """
        SELECT routine.*
        FROM routine
        INNER JOIN account_routine
            ON account_routine.routine_id = routine.id AND account_routine.account_id = :accountId
        WHERE account_routine.is_dirty = 1
        """,
    )
    suspend fun findPending(accountId: Uuid): List<RoutineLocalEntity>

    @Query(
        """
        UPDATE account_routine
        SET is_dirty = 0
        WHERE account_id = :accountId AND routine_id = :routineId
            AND EXISTS(
                SELECT 1
                FROM routine
                WHERE routine.id = :routineId AND routine.updated_at = :updatedAt
            )
        """,
    )
    suspend fun clearPending(
        accountId: Uuid,
        routineId: Uuid,
        updatedAt: Instant,
    ): Int
}
