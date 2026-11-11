package io.github.taetae98coding.diary.core.database.impl.qrtag.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Query
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entitytag.dao.AccountEntityTagDao
import io.github.taetae98coding.diary.core.database.impl.qrtag.entity.AccountQrTagLocalEntity
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
internal interface AccountQrTagDao : AccountEntityTagDao<AccountQrTagLocalEntity> {
    @Query(
        """
        SELECT tag.*
        FROM tag
        INNER JOIN account_tag
            ON account_tag.tag_id = tag.id AND account_tag.account_id = :accountId
        INNER JOIN qr_tag
            ON qr_tag.tag_id = tag.id
                AND qr_tag.qr_id = :entityId
                AND qr_tag.is_deleted = 0
        INNER JOIN account_qr_tag
            ON account_qr_tag.qr_id = qr_tag.qr_id
                AND account_qr_tag.tag_id = qr_tag.tag_id
                AND account_qr_tag.account_id = :accountId
        INNER JOIN qr
            ON qr.id = qr_tag.qr_id
        INNER JOIN account_qr
            ON account_qr.qr_id = qr.id AND account_qr.account_id = :accountId
        WHERE tag.is_deleted = 0
        ORDER BY tag.title ASC
        """,
    )
    override fun getTagList(
        accountId: Uuid,
        entityId: Uuid,
    ): Flow<List<TagLocalEntity>>

    @Query(
        """
        SELECT tag.*
        FROM tag
        INNER JOIN account_tag
            ON account_tag.tag_id = tag.id AND account_tag.account_id = :accountId
        WHERE tag.is_deleted = 0
            AND (
                tag.is_finished = 0
                OR EXISTS(
                    SELECT 1
                    FROM qr_tag
                    INNER JOIN account_qr_tag
                        ON account_qr_tag.qr_id = qr_tag.qr_id
                            AND account_qr_tag.tag_id = qr_tag.tag_id
                            AND account_qr_tag.account_id = :accountId
                    INNER JOIN account_qr
                        ON account_qr.qr_id = qr_tag.qr_id AND account_qr.account_id = :accountId
                    WHERE qr_tag.tag_id = tag.id
                        AND qr_tag.qr_id = :entityId
                        AND qr_tag.is_deleted = 0
                )
            )
            AND (
                :query = ''
                OR INSTR(LOWER(tag.emoji), LOWER(:query)) > 0
                OR INSTR(LOWER(tag.title), LOWER(:query)) > 0
                OR INSTR(LOWER(tag.description), LOWER(:query)) > 0
            )
        ORDER BY tag.title ASC
        """,
    )
    override fun pageSelectableTag(
        accountId: Uuid,
        entityId: Uuid,
        query: String,
    ): PagingSource<Int, TagLocalEntity>

    @Query(
        """
        UPDATE account_qr_tag
        SET is_dirty = 1
        WHERE account_id = :accountId AND qr_id = :entityId AND tag_id = :tagId
        """,
    )
    override suspend fun markPending(
        accountId: Uuid,
        entityId: Uuid,
        tagId: Uuid,
    ): Int
}
