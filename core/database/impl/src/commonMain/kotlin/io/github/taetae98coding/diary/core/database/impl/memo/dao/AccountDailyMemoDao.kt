package io.github.taetae98coding.diary.core.database.impl.memo.dao

import androidx.room3.Dao
import androidx.room3.Query
import io.github.taetae98coding.diary.core.database.api.memo.entity.DailyMemoLocalEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

@Dao
internal interface AccountDailyMemoDao {
    @Query(
        """
        SELECT memo.id AS id, memo.title AS title
        FROM memo
        INNER JOIN account_memo
            ON account_memo.memo_id = memo.id AND account_memo.account_id = :accountId
        WHERE memo.is_deleted = 0
            AND memo.is_finished = 0
            AND date(memo.start) <= date(:date)
            AND date(memo.end_inclusive) >= date(:date)
        ORDER BY memo.is_all_day DESC,
            memo.start ASC,
            CASE WHEN memo.is_all_day = 1 THEN memo.end_inclusive END DESC,
            CASE WHEN memo.is_all_day = 0 THEN memo.end_inclusive END ASC,
            memo.title ASC
        """,
    )
    fun get(
        accountId: Uuid,
        date: LocalDate,
    ): Flow<List<DailyMemoLocalEntity>>
}
