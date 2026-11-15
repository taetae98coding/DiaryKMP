package io.github.taetae98coding.diary.core.database.impl.routine.dao

import androidx.room3.Dao
import androidx.room3.Query
import io.github.taetae98coding.diary.core.database.api.routine.entity.CalendarRoutineRecordLocalEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

@Dao
internal interface AccountCalendarRoutineRecordDao {
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
        SELECT routine.id AS routine_id, routine.title AS title, routine.color AS color,
            routine_record.date AS date, SUM(routine_record.amount) AS amount, routine.unit AS unit
        FROM routine_record
        INNER JOIN account_routine_record
            ON account_routine_record.routine_record_id = routine_record.id
                AND account_routine_record.account_id = :accountId
        INNER JOIN routine
            ON routine.id = routine_record.routine_id AND routine.is_deleted = 0
        INNER JOIN account_routine
            ON account_routine.routine_id = routine.id AND account_routine.account_id = :accountId
        WHERE routine_record.is_deleted = 0
            AND routine_record.date >= :start
            AND routine_record.date <= :endInclusive
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
        GROUP BY routine.id, routine_record.date
        ORDER BY routine_record.date ASC, routine.title ASC, routine.id ASC
        """,
    )
    fun get(
        accountId: Uuid,
        start: LocalDate,
        endInclusive: LocalDate,
    ): Flow<List<CalendarRoutineRecordLocalEntity>>
}
