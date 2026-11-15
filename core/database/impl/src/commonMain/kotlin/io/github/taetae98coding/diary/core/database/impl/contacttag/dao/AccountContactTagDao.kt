package io.github.taetae98coding.diary.core.database.impl.contacttag.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Query
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.contacttag.entity.AccountContactTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entitytag.dao.AccountEntityTagDao
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
internal interface AccountContactTagDao : AccountEntityTagDao<AccountContactTagLocalEntity> {
    @Query(
        """
        SELECT tag.*
        FROM tag
        INNER JOIN account_tag
            ON account_tag.tag_id = tag.id AND account_tag.account_id = :accountId
        INNER JOIN contact_tag
            ON contact_tag.tag_id = tag.id
                AND contact_tag.contact_id = :entityId
                AND contact_tag.is_deleted = 0
        INNER JOIN account_contact_tag
            ON account_contact_tag.contact_id = contact_tag.contact_id
                AND account_contact_tag.tag_id = contact_tag.tag_id
                AND account_contact_tag.account_id = :accountId
        INNER JOIN contact
            ON contact.id = contact_tag.contact_id
        INNER JOIN account_contact
            ON account_contact.contact_id = contact.id AND account_contact.account_id = :accountId
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
                    FROM contact_tag
                    INNER JOIN account_contact_tag
                        ON account_contact_tag.contact_id = contact_tag.contact_id
                            AND account_contact_tag.tag_id = contact_tag.tag_id
                            AND account_contact_tag.account_id = :accountId
                    INNER JOIN account_contact
                        ON account_contact.contact_id = contact_tag.contact_id AND account_contact.account_id = :accountId
                    WHERE contact_tag.tag_id = tag.id
                        AND contact_tag.contact_id = :entityId
                        AND contact_tag.is_deleted = 0
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
        UPDATE account_contact_tag
        SET is_dirty = 1
        WHERE account_id = :accountId AND contact_id = :entityId AND tag_id = :tagId
        """,
    )
    override suspend fun markPending(
        accountId: Uuid,
        entityId: Uuid,
        tagId: Uuid,
    ): Int
}
