package io.github.taetae98coding.diary.core.database.impl.qrtag.dao

import androidx.room3.Dao
import androidx.room3.Query
import io.github.taetae98coding.diary.core.database.impl.entitytag.dao.AccountEntityTagSyncDao
import io.github.taetae98coding.diary.core.database.impl.qrtag.entity.AccountQrTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.qrtag.entity.QrTagLocalEntity
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
internal interface AccountQrTagSyncDao : AccountEntityTagSyncDao<QrTagLocalEntity, AccountQrTagLocalEntity> {
    @Query(
        """
        SELECT qr_tag.*
        FROM qr_tag
        INNER JOIN account_qr_tag
            ON account_qr_tag.qr_id = qr_tag.qr_id
                AND account_qr_tag.tag_id = qr_tag.tag_id
                AND account_qr_tag.account_id = :accountId
        WHERE account_qr_tag.is_dirty = 1
        """,
    )
    override suspend fun findPending(accountId: Uuid): List<QrTagLocalEntity>

    @Query(
        """
        UPDATE account_qr_tag
        SET is_dirty = 0
        WHERE account_id = :accountId AND qr_id = :entityId AND tag_id = :tagId
            AND EXISTS(
                SELECT 1
                FROM qr_tag
                WHERE qr_tag.qr_id = :entityId AND qr_tag.tag_id = :tagId AND qr_tag.updated_at = :updatedAt
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
