package io.github.taetae98coding.diary.core.database.impl.dao

import androidx.room3.Dao
import androidx.room3.Query
import io.github.taetae98coding.diary.core.database.api.memotag.entity.MemoTagLocalEntity
import io.github.taetae98coding.diary.library.room3.dao.RoomDao
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
internal interface MemoTagDao : RoomDao<MemoTagLocalEntity> {
    @Query(
        """
        SELECT *
        FROM memo_tag
        WHERE memo_id IN (:memoIdList)
        """,
    )
    suspend fun findByMemoIdList(memoIdList: List<Uuid>): List<MemoTagLocalEntity>

    @Query(
        """
        UPDATE memo_tag
        SET is_deleted = :isDeleted, updated_at = :updatedAt
        WHERE memo_id = :memoId AND tag_id = :tagId
        """,
    )
    suspend fun updateDeleted(
        memoId: Uuid,
        tagId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int
}
