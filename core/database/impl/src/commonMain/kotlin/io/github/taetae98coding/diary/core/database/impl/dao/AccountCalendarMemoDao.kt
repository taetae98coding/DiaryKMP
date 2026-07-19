package io.github.taetae98coding.diary.core.database.impl.dao

import androidx.room3.Dao
import androidx.room3.Query
import io.github.taetae98coding.diary.core.database.api.memo.entity.CalendarMemoLocalEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

@Dao
internal interface AccountCalendarMemoDao {
    @Query(
        """
        WITH calendar_filter_selected_tag AS (
            SELECT calendar_filter_tag.tag_id
            FROM calendar_filter_tag
            INNER JOIN tag
                ON tag.id = calendar_filter_tag.tag_id
                    AND tag.is_finished = 0
                    AND tag.is_deleted = 0
            INNER JOIN account_tag
                ON account_tag.tag_id = calendar_filter_tag.tag_id AND account_tag.account_id = :accountId
            WHERE calendar_filter_tag.account_id = :accountId
        )
        SELECT memo.id AS id, memo.title AS title,
            COALESCE(tag.color, memo.color) AS color,
            memo.is_all_day AS is_all_day, memo.start AS start, memo.end_inclusive AS end_inclusive
        FROM memo
        INNER JOIN account_memo
            ON account_memo.memo_id = memo.id AND account_memo.account_id = :accountId
        LEFT JOIN tag
            ON tag.id = memo.primary_tag_id AND tag.is_deleted = 0
        WHERE memo.is_deleted = 0
            AND date(memo.start) <= date(:endInclusive)
            AND date(memo.end_inclusive) >= date(:start)
            AND (
                NOT EXISTS(SELECT 1 FROM calendar_filter_selected_tag)
                OR EXISTS(
                    SELECT 1
                    FROM memo_tag
                    INNER JOIN account_memo_tag
                        ON account_memo_tag.memo_id = memo_tag.memo_id
                            AND account_memo_tag.tag_id = memo_tag.tag_id
                            AND account_memo_tag.account_id = :accountId
                    WHERE memo_tag.memo_id = memo.id
                        AND memo_tag.is_deleted = 0
                        AND memo_tag.tag_id IN (SELECT tag_id FROM calendar_filter_selected_tag)
                )
            )
        ORDER BY memo.is_all_day DESC,
            memo.start ASC,
            CASE WHEN memo.is_all_day = 1 THEN memo.end_inclusive END DESC,
            CASE WHEN memo.is_all_day = 0 THEN memo.end_inclusive END ASC,
            memo.title ASC
        """,
    )
    fun get(
        accountId: Uuid,
        start: LocalDate,
        endInclusive: LocalDate,
    ): Flow<List<CalendarMemoLocalEntity>>
}
