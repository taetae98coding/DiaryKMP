package io.github.taetae98coding.diary.core.database.impl.dday.dao

import androidx.room3.Dao
import androidx.room3.Query
import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayLocalEntity
import io.github.taetae98coding.diary.core.database.impl.dday.entity.AccountDDayLocalEntity
import io.github.taetae98coding.diary.library.room3.dao.RoomDao
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
internal interface AccountDDaySyncDao : RoomDao<AccountDDayLocalEntity> {
    @Query(
        """
        SELECT dday.*
        FROM dday
        INNER JOIN account_dday
            ON account_dday.dday_id = dday.id AND account_dday.account_id = :accountId
        WHERE account_dday.is_dirty = 1
        """,
    )
    suspend fun findPending(accountId: Uuid): List<DDayLocalEntity>

    @Query(
        """
        UPDATE account_dday
        SET is_dirty = 0
        WHERE account_id = :accountId AND dday_id = :ddayId
            AND EXISTS(
                SELECT 1
                FROM dday
                WHERE dday.id = :ddayId AND dday.updated_at = :updatedAt
            )
        """,
    )
    suspend fun clearPending(
        accountId: Uuid,
        ddayId: Uuid,
        updatedAt: Instant,
    ): Int
}
