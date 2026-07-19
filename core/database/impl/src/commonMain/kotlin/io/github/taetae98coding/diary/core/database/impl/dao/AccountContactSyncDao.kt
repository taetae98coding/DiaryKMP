package io.github.taetae98coding.diary.core.database.impl.dao

import androidx.room3.Dao
import androidx.room3.Query
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entity.AccountContactLocalEntity
import io.github.taetae98coding.diary.library.room3.dao.RoomDao
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
internal interface AccountContactSyncDao : RoomDao<AccountContactLocalEntity> {
    @Query(
        """
        SELECT contact.*
        FROM contact
        INNER JOIN account_contact
            ON account_contact.contact_id = contact.id AND account_contact.account_id = :accountId
        WHERE account_contact.is_dirty = 1
        """,
    )
    suspend fun findPending(accountId: Uuid): List<ContactLocalEntity>

    @Query(
        """
        UPDATE account_contact
        SET is_dirty = 0
        WHERE account_id = :accountId AND contact_id = :contactId
            AND EXISTS(
                SELECT 1
                FROM contact
                WHERE contact.id = :contactId AND contact.updated_at = :updatedAt
            )
        """,
    )
    suspend fun clearPending(
        accountId: Uuid,
        contactId: Uuid,
        updatedAt: Instant,
    ): Int
}
