package io.github.taetae98coding.diary.core.database.impl.dao

import androidx.room3.Dao
import androidx.room3.Query
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entity.AccountMemoPlaceLocalEntity
import io.github.taetae98coding.diary.library.room3.dao.RoomDao
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

@Dao
internal interface AccountMemoPlaceDao : RoomDao<AccountMemoPlaceLocalEntity> {
    @Query(
        """
        SELECT place.*
        FROM place
        INNER JOIN account_place
            ON account_place.place_id = place.id AND account_place.account_id = :accountId
        INNER JOIN memo_place
            ON memo_place.place_id = place.id
                AND memo_place.memo_id = :memoId
                AND memo_place.is_deleted = 0
        INNER JOIN account_memo_place
            ON account_memo_place.memo_id = memo_place.memo_id
                AND account_memo_place.place_id = memo_place.place_id
                AND account_memo_place.account_id = :accountId
        INNER JOIN memo
            ON memo.id = memo_place.memo_id
        WHERE place.is_deleted = 0
        ORDER BY place.title ASC
        """,
    )
    fun getPlaceList(
        accountId: Uuid,
        memoId: Uuid,
    ): Flow<List<PlaceLocalEntity>>

    @Query(
        """
        UPDATE account_memo_place
        SET is_dirty = 1
        WHERE account_id = :accountId AND memo_id = :memoId AND place_id = :placeId
        """,
    )
    suspend fun markPending(
        accountId: Uuid,
        memoId: Uuid,
        placeId: Uuid,
    ): Int
}
