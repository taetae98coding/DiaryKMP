package io.github.taetae98coding.diary.core.database.impl.memocontact.dao

import androidx.room3.Dao
import androidx.room3.Query
import io.github.taetae98coding.diary.core.database.api.memocontact.entity.MemoContactLocalEntity
import io.github.taetae98coding.diary.core.database.impl.memocontact.entity.AccountMemoContactLocalEntity
import io.github.taetae98coding.diary.library.room3.dao.RoomDao
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
internal interface AccountMemoContactSyncDao : RoomDao<AccountMemoContactLocalEntity> {
    @Query(
        """
        SELECT memo_contact.*
        FROM memo_contact
        INNER JOIN account_memo_contact
            ON account_memo_contact.memo_id = memo_contact.memo_id
                AND account_memo_contact.contact_id = memo_contact.contact_id
                AND account_memo_contact.account_id = :accountId
        WHERE account_memo_contact.is_dirty = 1
        """,
    )
    suspend fun findPending(accountId: Uuid): List<MemoContactLocalEntity>

    @Query(
        """
        UPDATE account_memo_contact
        SET is_dirty = 0
        WHERE account_id = :accountId AND memo_id = :memoId AND contact_id = :contactId
            AND EXISTS(
                SELECT 1
                FROM memo_contact
                WHERE memo_contact.memo_id = :memoId AND memo_contact.contact_id = :contactId AND memo_contact.updated_at = :updatedAt
            )
        """,
    )
    suspend fun clearPending(
        accountId: Uuid,
        memoId: Uuid,
        contactId: Uuid,
        updatedAt: Instant,
    ): Int
}
