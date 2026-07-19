package io.github.taetae98coding.diary.core.database.impl.dao

import androidx.room3.Dao
import androidx.room3.Query
import io.github.taetae98coding.diary.core.database.api.memoplace.entity.MemoPlaceLocalEntity
import io.github.taetae98coding.diary.library.room3.dao.RoomDao
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
internal interface MemoPlaceDao : RoomDao<MemoPlaceLocalEntity> {
    @Query(
        """
        SELECT *
        FROM memo_place
        WHERE memo_id IN (:memoIdList)
        """,
    )
    suspend fun findByMemoIdList(memoIdList: List<Uuid>): List<MemoPlaceLocalEntity>

    @Query(
        """
        UPDATE memo_place
        SET is_deleted = :isDeleted, updated_at = :updatedAt
        WHERE memo_id = :memoId AND place_id = :placeId
        """,
    )
    suspend fun updateDeleted(
        memoId: Uuid,
        placeId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int
}
