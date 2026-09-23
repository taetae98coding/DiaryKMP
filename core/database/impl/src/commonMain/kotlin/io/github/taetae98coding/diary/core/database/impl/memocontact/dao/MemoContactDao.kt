package io.github.taetae98coding.diary.core.database.impl.memocontact.dao

import androidx.room3.Dao
import androidx.room3.Query
import io.github.taetae98coding.diary.core.database.api.memocontact.entity.MemoContactLocalEntity
import io.github.taetae98coding.diary.library.room3.dao.RoomDao
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
internal interface MemoContactDao : RoomDao<MemoContactLocalEntity> {
    @Query(
        """
        SELECT *
        FROM memo_contact
        WHERE memo_id IN (:memoIdList)
        """,
    )
    suspend fun findByMemoIdList(memoIdList: List<Uuid>): List<MemoContactLocalEntity>

    @Query(
        """
        UPDATE memo_contact
        SET is_deleted = :isDeleted, updated_at = :updatedAt
        WHERE memo_id = :memoId AND contact_id = :contactId
        """,
    )
    suspend fun updateDeleted(
        memoId: Uuid,
        contactId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int
}
