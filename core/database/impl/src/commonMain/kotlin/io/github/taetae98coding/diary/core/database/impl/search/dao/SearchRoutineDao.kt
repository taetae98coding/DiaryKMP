package io.github.taetae98coding.diary.core.database.impl.search.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Query
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineCycleLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineSummaryLocalEntity
import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
internal interface SearchRoutineDao {
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
        WHERE routine.is_deleted = 0
            AND (
                INSTR(LOWER(routine.title), LOWER(:query)) > 0
                OR INSTR(LOWER(routine.description), LOWER(:query)) > 0
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
        query: String,
        today: LocalDate,
        weekStart: LocalDate,
        weekEndInclusive: LocalDate,
        monthStart: LocalDate,
        monthEndInclusive: LocalDate,
        weeklyCycle: RoutineCycleLocalEntity,
        monthlyCycle: RoutineCycleLocalEntity,
    ): PagingSource<Int, RoutineSummaryLocalEntity>
}
