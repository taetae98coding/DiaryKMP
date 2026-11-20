package io.github.taetae98coding.diary.core.database.impl.qr.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Query
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import io.github.taetae98coding.diary.core.database.api.qr.entity.QrLocalEntity
import io.github.taetae98coding.diary.core.database.impl.tag.dao.SCOPED_TAG_CTE
import kotlin.uuid.Uuid

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
internal interface AccountTagQrDao {
    @Query(
        SCOPED_TAG_CTE +
            """
        SELECT qr.*
        FROM qr
        INNER JOIN account_qr
            ON account_qr.qr_id = qr.id AND account_qr.account_id = :accountId
        WHERE qr.is_deleted = 0
            AND EXISTS(
                SELECT 1
                FROM qr_tag
                INNER JOIN scoped_tag
                    ON scoped_tag.tag_id = qr_tag.tag_id
                INNER JOIN account_qr_tag
                    ON account_qr_tag.qr_id = qr_tag.qr_id
                        AND account_qr_tag.tag_id = qr_tag.tag_id
                        AND account_qr_tag.account_id = :accountId
                INNER JOIN tag
                    ON tag.id = qr_tag.tag_id
                INNER JOIN account_tag
                    ON account_tag.tag_id = tag.id AND account_tag.account_id = :accountId
                WHERE qr_tag.qr_id = qr.id
                    AND qr_tag.is_deleted = 0
            )
        ORDER BY qr.title ASC
        """,
    )
    fun page(
        accountId: Uuid,
        tagId: Uuid,
        scope: String,
    ): PagingSource<Int, QrLocalEntity>
}
