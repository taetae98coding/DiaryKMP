package io.github.taetae98coding.diary.core.database.impl.memoweb.dao

import androidx.room3.Dao
import androidx.room3.Query
import io.github.taetae98coding.diary.core.database.api.memoweb.entity.MemoWebLocalEntity
import io.github.taetae98coding.diary.library.room3.dao.RoomDao
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
internal interface MemoWebDao : RoomDao<MemoWebLocalEntity> {
    @Query(
        """
        SELECT *
        FROM memo_web
        WHERE memo_id IN (:memoIdList)
        """,
    )
    suspend fun findByMemoIdList(memoIdList: List<Uuid>): List<MemoWebLocalEntity>

    @Query(
        """
        UPDATE memo_web
        SET is_deleted = :isDeleted, updated_at = :updatedAt
        WHERE memo_id = :memoId AND web_id = :webId
        """,
    )
    suspend fun updateDeleted(
        memoId: Uuid,
        webId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int
}
