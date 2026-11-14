package io.github.taetae98coding.diary.core.database.impl.routine.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Query
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineCycleLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineSummaryLocalEntity
import io.github.taetae98coding.diary.core.database.impl.routine.entity.AccountRoutineLocalEntity
import io.github.taetae98coding.diary.library.room3.dao.RoomDao
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
internal interface AccountRoutineDao : RoomDao<AccountRoutineLocalEntity> {
    @Query(
        """
        WITH routine_filter_selected_tag AS (
            SELECT routine_filter_tag.tag_id
            FROM routine_filter_tag
            INNER JOIN tag
                ON tag.id = routine_filter_tag.tag_id
                    AND tag.is_finished = 0
                    AND tag.is_deleted = 0
            INNER JOIN account_tag
                ON account_tag.tag_id = routine_filter_tag.tag_id AND account_tag.account_id = :accountId
            WHERE routine_filter_tag.account_id = :accountId
        )
        SELECT routine.*,
            COALESCE((
                SELECT SUM(routine_record.amount)
                FROM routine_record
                INNER JOIN account_routine_record
                    ON account_routine_record.routine_record_id = routine_record.id
                        AND account_routine_record.account_id = :accountId
                WHERE routine_record.routine_id = routine.id
                    AND routine_record.is_deleted = 0
                    AND routine_record.date >= CASE routine.cycle
                        WHEN :weeklyCycle THEN :weekStart
                        WHEN :monthlyCycle THEN :monthStart
                        ELSE :today
                    END
                    AND routine_record.date <= CASE routine.cycle
                        WHEN :weeklyCycle THEN :weekEndInclusive
                        WHEN :monthlyCycle THEN :monthEndInclusive
                        ELSE :today
                    END
            ), 0) AS cycle_amount
        FROM routine
        INNER JOIN account_routine
            ON account_routine.routine_id = routine.id AND account_routine.account_id = :accountId
        WHERE routine.is_finished = 0
            AND routine.is_deleted = 0
            AND (:isStartedOnly = 0 OR routine.start_date <= :today)
            AND routine.cycle IN (:cycleList)
            AND (
                NOT EXISTS(SELECT 1 FROM routine_filter_selected_tag)
                OR EXISTS(
                    SELECT 1
                    FROM routine_tag
                    INNER JOIN account_routine_tag
                        ON account_routine_tag.routine_id = routine_tag.routine_id
                            AND account_routine_tag.tag_id = routine_tag.tag_id
                            AND account_routine_tag.account_id = :accountId
                    WHERE routine_tag.routine_id = routine.id
                        AND routine_tag.is_deleted = 0
                        AND routine_tag.tag_id IN (SELECT tag_id FROM routine_filter_selected_tag)
                )
            )
        ORDER BY
            CASE
                WHEN :today < routine.start_date THEN 1
                ELSE 0
            END ASC,
            routine.title ASC
        """,
    )
    @Suppress("LongParameterList")
    fun page(
        accountId: Uuid,
        today: LocalDate,
        weekStart: LocalDate,
        weekEndInclusive: LocalDate,
        monthStart: LocalDate,
        monthEndInclusive: LocalDate,
        weeklyCycle: RoutineCycleLocalEntity,
        monthlyCycle: RoutineCycleLocalEntity,
        isStartedOnly: Boolean,
        cycleList: List<String>,
    ): PagingSource<Int, RoutineSummaryLocalEntity>

    @Query(
        """
        SELECT routine.*,
            COALESCE((
                SELECT SUM(routine_record.amount)
                FROM routine_record
                INNER JOIN account_routine_record
                    ON account_routine_record.routine_record_id = routine_record.id
                        AND account_routine_record.account_id = :accountId
                WHERE routine_record.routine_id = routine.id
                    AND routine_record.is_deleted = 0
                    AND routine_record.date >= CASE routine.cycle
                        WHEN :weeklyCycle THEN :weekStart
                        WHEN :monthlyCycle THEN :monthStart
                        ELSE :today
                    END
                    AND routine_record.date <= CASE routine.cycle
                        WHEN :weeklyCycle THEN :weekEndInclusive
                        WHEN :monthlyCycle THEN :monthEndInclusive
                        ELSE :today
                    END
            ), 0) AS cycle_amount
        FROM routine
        INNER JOIN account_routine
            ON account_routine.routine_id = routine.id AND account_routine.account_id = :accountId
        WHERE routine.is_finished = 1
            AND routine.is_deleted = 0
        ORDER BY
            CASE
                WHEN :today < routine.start_date THEN 1
                ELSE 0
            END ASC,
            routine.title ASC
        """,
    )
    @Suppress("LongParameterList")
    fun pageFinished(
        accountId: Uuid,
        today: LocalDate,
        weekStart: LocalDate,
        weekEndInclusive: LocalDate,
        monthStart: LocalDate,
        monthEndInclusive: LocalDate,
        weeklyCycle: RoutineCycleLocalEntity,
        monthlyCycle: RoutineCycleLocalEntity,
    ): PagingSource<Int, RoutineSummaryLocalEntity>

    @Query(
        """
        SELECT routine.*
        FROM routine
        INNER JOIN account_routine
            ON account_routine.routine_id = routine.id AND account_routine.account_id = :accountId
        WHERE routine.id = :routineId
        """,
    )
    fun find(
        accountId: Uuid,
        routineId: Uuid,
    ): Flow<RoutineLocalEntity?>

    @Query(
        """
        SELECT routine.*,
            COALESCE((
                SELECT SUM(routine_record.amount)
                FROM routine_record
                INNER JOIN account_routine_record
                    ON account_routine_record.routine_record_id = routine_record.id
                        AND account_routine_record.account_id = :accountId
                WHERE routine_record.routine_id = routine.id
                    AND routine_record.is_deleted = 0
                    AND routine_record.date >= CASE routine.cycle
                        WHEN :weeklyCycle THEN :weekStart
                        WHEN :monthlyCycle THEN :monthStart
                        ELSE :today
                    END
                    AND routine_record.date <= CASE routine.cycle
                        WHEN :weeklyCycle THEN :weekEndInclusive
                        WHEN :monthlyCycle THEN :monthEndInclusive
                        ELSE :today
                    END
            ), 0) AS cycle_amount
        FROM routine
        INNER JOIN account_routine
            ON account_routine.routine_id = routine.id AND account_routine.account_id = :accountId
        WHERE routine.id = :routineId
        """,
    )
    @Suppress("LongParameterList")
    fun findSummary(
        accountId: Uuid,
        routineId: Uuid,
        today: LocalDate,
        weekStart: LocalDate,
        weekEndInclusive: LocalDate,
        monthStart: LocalDate,
        monthEndInclusive: LocalDate,
        weeklyCycle: RoutineCycleLocalEntity,
        monthlyCycle: RoutineCycleLocalEntity,
    ): Flow<RoutineSummaryLocalEntity?>

    @Query(
        """
        SELECT EXISTS(
            SELECT 1
            FROM routine
            INNER JOIN account_routine
                ON account_routine.routine_id = routine.id AND account_routine.account_id = :accountId
            WHERE routine.is_finished = 0 AND routine.is_deleted = 0
        )
        """,
    )
    fun getUnfinishedExists(accountId: Uuid): Flow<Boolean>

    @Query(
        """
        SELECT EXISTS(
            SELECT 1
            FROM routine
            INNER JOIN account_routine
                ON account_routine.routine_id = routine.id AND account_routine.account_id = :accountId
            WHERE routine.id = :routineId
        )
        """,
    )
    suspend fun exists(
        accountId: Uuid,
        routineId: Uuid,
    ): Boolean

    @Query(
        """
        UPDATE routine
        SET title = :title,
            description = :description,
            color = :color,
            start_date = :startDate,
            cycle = :cycle,
            unit = :unit,
            goal_amount = :goalAmount,
            amount_per_record = :amountPerRecord,
            updated_at = :updatedAt
        WHERE id = :routineId
            AND EXISTS(
                SELECT 1
                FROM account_routine
                WHERE account_routine.routine_id = routine.id AND account_routine.account_id = :accountId
            )
        """,
    )
    @Suppress("LongParameterList")
    suspend fun updateDetail(
        accountId: Uuid,
        routineId: Uuid,
        title: String,
        description: String,
        color: Long,
        startDate: LocalDate,
        cycle: RoutineCycleLocalEntity,
        unit: String,
        goalAmount: Int?,
        amountPerRecord: Int,
        updatedAt: Instant,
    ): Int

    @Query(
        """
        UPDATE routine
        SET is_finished = :isFinished, updated_at = :updatedAt
        WHERE id = :routineId
            AND EXISTS(
                SELECT 1
                FROM account_routine
                WHERE account_routine.routine_id = routine.id AND account_routine.account_id = :accountId
            )
        """,
    )
    suspend fun updateFinished(
        accountId: Uuid,
        routineId: Uuid,
        isFinished: Boolean,
        updatedAt: Instant,
    ): Int

    @Query(
        """
        UPDATE routine
        SET is_deleted = :isDeleted, updated_at = :updatedAt
        WHERE id = :routineId
            AND EXISTS(
                SELECT 1
                FROM account_routine
                WHERE account_routine.routine_id = routine.id AND account_routine.account_id = :accountId
            )
        """,
    )
    suspend fun updateDeleted(
        accountId: Uuid,
        routineId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int

    @Query(
        """
        UPDATE account_routine
        SET is_dirty = 1
        WHERE account_id = :accountId AND routine_id = :routineId
        """,
    )
    suspend fun markPending(
        accountId: Uuid,
        routineId: Uuid,
    ): Int
}
