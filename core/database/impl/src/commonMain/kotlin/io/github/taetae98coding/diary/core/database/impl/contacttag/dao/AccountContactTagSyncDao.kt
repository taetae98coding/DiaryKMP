package io.github.taetae98coding.diary.core.database.impl.contacttag.dao

import androidx.room3.Dao
import androidx.room3.Query
import io.github.taetae98coding.diary.core.database.impl.contacttag.entity.AccountContactTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.contacttag.entity.ContactTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entitytag.dao.AccountEntityTagSyncDao
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
internal interface AccountContactTagSyncDao : AccountEntityTagSyncDao<ContactTagLocalEntity, AccountContactTagLocalEntity> {
    @Query(
        """
        SELECT contact_tag.*
        FROM contact_tag
        INNER JOIN account_contact_tag
            ON account_contact_tag.contact_id = contact_tag.contact_id
                AND account_contact_tag.tag_id = contact_tag.tag_id
                AND account_contact_tag.account_id = :accountId
        WHERE account_contact_tag.is_dirty = 1
        """,
    )
    override suspend fun findPending(accountId: Uuid): List<ContactTagLocalEntity>

    @Query(
        """
        UPDATE account_contact_tag
        SET is_dirty = 0
        WHERE account_id = :accountId AND contact_id = :entityId AND tag_id = :tagId
            AND EXISTS(
                SELECT 1
                FROM contact_tag
                WHERE contact_tag.contact_id = :entityId AND contact_tag.tag_id = :tagId AND contact_tag.updated_at = :updatedAt
            )
        """,
    )
    override suspend fun clearPending(
        accountId: Uuid,
        entityId: Uuid,
        tagId: Uuid,
        updatedAt: Instant,
    ): Int
}
