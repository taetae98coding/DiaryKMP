package io.github.taetae98coding.diary.core.database.impl.memoplace.dao

import androidx.room3.Dao
import androidx.room3.Query
import io.github.taetae98coding.diary.core.database.api.memoplace.entity.MemoPlaceLocalEntity
import io.github.taetae98coding.diary.core.database.impl.memoplace.entity.AccountMemoPlaceLocalEntity
import io.github.taetae98coding.diary.library.room3.dao.RoomDao
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
internal interface AccountMemoPlaceSyncDao : RoomDao<AccountMemoPlaceLocalEntity> {
    @Query(
        """
        SELECT memo_place.*
        FROM memo_place
        INNER JOIN account_memo_place
            ON account_memo_place.memo_id = memo_place.memo_id
                AND account_memo_place.place_id = memo_place.place_id
                AND account_memo_place.account_id = :accountId
        WHERE account_memo_place.is_dirty = 1
        """,
    )
    suspend fun findPending(accountId: Uuid): List<MemoPlaceLocalEntity>

    @Query(
        """
        UPDATE account_memo_place
        SET is_dirty = 0
        WHERE account_id = :accountId AND memo_id = :memoId AND place_id = :placeId
            AND EXISTS(
                SELECT 1
                FROM memo_place
                WHERE memo_place.memo_id = :memoId AND memo_place.place_id = :placeId AND memo_place.updated_at = :updatedAt
            )
        """,
    )
    suspend fun clearPending(
        accountId: Uuid,
        memoId: Uuid,
        placeId: Uuid,
        updatedAt: Instant,
    ): Int
}
