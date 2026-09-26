package io.github.taetae98coding.diary.core.database.impl.qr.dao

import androidx.room3.Dao
import androidx.room3.Query
import io.github.taetae98coding.diary.core.database.api.qr.entity.QrLocalEntity
import io.github.taetae98coding.diary.core.database.impl.qr.entity.AccountQrLocalEntity
import io.github.taetae98coding.diary.library.room3.dao.RoomDao
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
internal interface AccountQrSyncDao : RoomDao<AccountQrLocalEntity> {
    @Query(
        """
        SELECT qr.*
        FROM qr
        INNER JOIN account_qr
            ON account_qr.qr_id = qr.id AND account_qr.account_id = :accountId
        WHERE account_qr.is_dirty = 1
        """,
    )
    suspend fun findPending(accountId: Uuid): List<QrLocalEntity>

    @Query(
        """
        UPDATE account_qr
        SET is_dirty = 0
        WHERE account_id = :accountId AND qr_id = :qrId
            AND EXISTS(
                SELECT 1
                FROM qr
                WHERE qr.id = :qrId AND qr.updated_at = :updatedAt
            )
        """,
    )
    suspend fun clearPending(
        accountId: Uuid,
        qrId: Uuid,
        updatedAt: Instant,
    ): Int
}
