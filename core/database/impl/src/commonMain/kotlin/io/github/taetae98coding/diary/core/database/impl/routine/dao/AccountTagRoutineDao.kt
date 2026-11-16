package io.github.taetae98coding.diary.core.database.impl.routine.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Query
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineCycleLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineSummaryLocalEntity
import io.github.taetae98coding.diary.core.database.impl.tag.dao.SCOPED_TAG_CTE
import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
internal interface AccountTagRoutineDao {
    @Query(
        SCOPED_TAG_CTE +
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
        WHERE routine.is_finished = 0
            AND routine.is_deleted = 0
            AND EXISTS(
                SELECT 1
                FROM routine_tag
                INNER JOIN scoped_tag
                    ON scoped_tag.tag_id = routine_tag.tag_id
                INNER JOIN account_routine_tag
                    ON account_routine_tag.routine_id = routine_tag.routine_id
                        AND account_routine_tag.tag_id = routine_tag.tag_id
                        AND account_routine_tag.account_id = :accountId
                INNER JOIN tag
                    ON tag.id = routine_tag.tag_id
                INNER JOIN account_tag
                    ON account_tag.tag_id = tag.id AND account_tag.account_id = :accountId
                WHERE routine_tag.routine_id = routine.id
                    AND routine_tag.is_deleted = 0
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
        tagId: Uuid,
        scope: String,
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
            AND EXISTS(
                SELECT 1
                FROM routine_tag
                INNER JOIN account_routine_tag
                    ON account_routine_tag.routine_id = routine_tag.routine_id
                        AND account_routine_tag.tag_id = routine_tag.tag_id
                        AND account_routine_tag.account_id = :accountId
                INNER JOIN account_tag
                    ON account_tag.tag_id = routine_tag.tag_id AND account_tag.account_id = :accountId
                WHERE routine_tag.routine_id = routine.id
                    AND routine_tag.tag_id = :tagId
                    AND routine_tag.is_deleted = 0
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
    fun pageFinished(
        accountId: Uuid,
        tagId: Uuid,
        today: LocalDate,
        weekStart: LocalDate,
        weekEndInclusive: LocalDate,
        monthStart: LocalDate,
        monthEndInclusive: LocalDate,
        weeklyCycle: RoutineCycleLocalEntity,
        monthlyCycle: RoutineCycleLocalEntity,
    ): PagingSource<Int, RoutineSummaryLocalEntity>
}
