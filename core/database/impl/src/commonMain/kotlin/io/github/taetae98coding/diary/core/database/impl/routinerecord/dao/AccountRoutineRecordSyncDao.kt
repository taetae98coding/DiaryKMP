package io.github.taetae98coding.diary.core.database.impl.routinerecord.dao

import androidx.room3.Dao
import androidx.room3.Query
import io.github.taetae98coding.diary.core.database.api.routinerecord.entity.RoutineRecordLocalEntity
import io.github.taetae98coding.diary.core.database.impl.routinerecord.entity.AccountRoutineRecordLocalEntity
import io.github.taetae98coding.diary.library.room3.dao.RoomDao
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
internal interface AccountRoutineRecordSyncDao : RoomDao<AccountRoutineRecordLocalEntity> {
    @Query(
        """
        SELECT routine_record.*
        FROM routine_record
        INNER JOIN account_routine_record
            ON account_routine_record.routine_record_id = routine_record.id AND account_routine_record.account_id = :accountId
        WHERE account_routine_record.is_dirty = 1
        """,
    )
    suspend fun findPending(accountId: Uuid): List<RoutineRecordLocalEntity>

    @Query(
        """
        UPDATE account_routine_record
        SET is_dirty = 0
        WHERE account_id = :accountId AND routine_record_id = :recordId
            AND EXISTS(
                SELECT 1
                FROM routine_record
                WHERE routine_record.id = :recordId AND routine_record.updated_at = :updatedAt
            )
        """,
    )
    suspend fun clearPending(
        accountId: Uuid,
        recordId: Uuid,
        updatedAt: Instant,
    ): Int
}
