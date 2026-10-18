package io.github.taetae98coding.diary.core.database.impl.qr.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Query
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import io.github.taetae98coding.diary.core.database.api.qr.entity.QrLocalEntity
import io.github.taetae98coding.diary.core.database.impl.qr.entity.AccountQrLocalEntity
import io.github.taetae98coding.diary.library.room3.dao.RoomDao
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
internal interface AccountQrDao : RoomDao<AccountQrLocalEntity> {
    @Query(
        """
        SELECT qr.*
        FROM qr
        INNER JOIN account_qr
            ON account_qr.qr_id = qr.id AND account_qr.account_id = :accountId
        WHERE qr.is_deleted = 0
        ORDER BY qr.title ASC
        """,
    )
    fun page(accountId: Uuid): PagingSource<Int, QrLocalEntity>

    @Query(
        """
        SELECT qr.*
        FROM qr
        INNER JOIN account_qr
            ON account_qr.qr_id = qr.id AND account_qr.account_id = :accountId
        WHERE qr.id = :qrId
        """,
    )
    fun find(
        accountId: Uuid,
        qrId: Uuid,
    ): Flow<QrLocalEntity?>

    @Query(
        """
        UPDATE qr
        SET title = :title, description = :description, value = :value, updated_at = :updatedAt
        WHERE id = :qrId
            AND EXISTS(
                SELECT 1
                FROM account_qr
                WHERE account_qr.qr_id = qr.id AND account_qr.account_id = :accountId
            )
        """,
    )
    suspend fun updateDetail(
        accountId: Uuid,
        qrId: Uuid,
        title: String,
        description: String,
        value: String,
        updatedAt: Instant,
    ): Int

    @Query(
        """
        UPDATE qr
        SET is_deleted = :isDeleted, updated_at = :updatedAt
        WHERE id = :qrId
            AND EXISTS(
                SELECT 1
                FROM account_qr
                WHERE account_qr.qr_id = qr.id AND account_qr.account_id = :accountId
            )
        """,
    )
    suspend fun updateDeleted(
        accountId: Uuid,
        qrId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int

    @Query(
        """
        UPDATE account_qr
        SET is_dirty = 1
        WHERE account_id = :accountId AND qr_id = :qrId
        """,
    )
    suspend fun markPending(
        accountId: Uuid,
        qrId: Uuid,
    ): Int
}
