package io.github.taetae98coding.diary.core.database.impl.routinerecord.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Query
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import io.github.taetae98coding.diary.core.database.api.routinerecord.entity.RoutineRecordLocalEntity
import io.github.taetae98coding.diary.core.database.impl.routinerecord.entity.AccountRoutineRecordLocalEntity
import io.github.taetae98coding.diary.library.room3.dao.RoomDao
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
internal interface AccountRoutineRecordDao : RoomDao<AccountRoutineRecordLocalEntity> {
    @Query(
        """
        SELECT routine_record.*
        FROM routine_record
        INNER JOIN account_routine_record
            ON account_routine_record.routine_record_id = routine_record.id
                AND account_routine_record.account_id = :accountId
        INNER JOIN routine
            ON routine.id = routine_record.routine_id AND routine.is_deleted = 0
        INNER JOIN account_routine
            ON account_routine.routine_id = routine.id AND account_routine.account_id = :accountId
        WHERE routine_record.routine_id = :routineId
            AND routine_record.is_deleted = 0
        ORDER BY routine_record.date DESC, routine_record.created_at DESC, routine_record.id ASC
        """,
    )
    fun page(
        accountId: Uuid,
        routineId: Uuid,
    ): PagingSource<Int, RoutineRecordLocalEntity>

    @Query(
        """
        SELECT routine_record.*
        FROM routine_record
        INNER JOIN account_routine_record
            ON account_routine_record.routine_record_id = routine_record.id
                AND account_routine_record.account_id = :accountId
        WHERE routine_record.id = :recordId
        """,
    )
    fun find(
        accountId: Uuid,
        recordId: Uuid,
    ): Flow<RoutineRecordLocalEntity?>

    @Query(
        """
        UPDATE routine_record
        SET is_deleted = :isDeleted, updated_at = :updatedAt
        WHERE id = :recordId
            AND EXISTS(
                SELECT 1
                FROM account_routine_record
                WHERE account_routine_record.routine_record_id = routine_record.id
                    AND account_routine_record.account_id = :accountId
            )
        """,
    )
    suspend fun updateDeleted(
        accountId: Uuid,
        recordId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int

    @Query(
        """
        UPDATE account_routine_record
        SET is_dirty = 1
        WHERE account_id = :accountId AND routine_record_id = :recordId
        """,
    )
    suspend fun markPending(
        accountId: Uuid,
        recordId: Uuid,
    ): Int
}
