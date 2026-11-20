package io.github.taetae98coding.diary.core.database.impl.contact.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Query
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactListItemLocalEntity
import io.github.taetae98coding.diary.core.database.impl.tag.dao.SCOPED_TAG_CTE
import kotlin.uuid.Uuid

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
internal interface AccountTagContactDao {
    @Query(
        SCOPED_TAG_CTE +
            """
        SELECT contact.*,
            CASE
                WHEN EXISTS(
                    SELECT 1
                    FROM contact AS same_name_contact
                    INNER JOIN account_contact AS same_name_account_contact
                        ON same_name_account_contact.contact_id = same_name_contact.id
                            AND same_name_account_contact.account_id = :accountId
                    WHERE same_name_contact.name = contact.name
                        AND same_name_contact.id != contact.id
                        AND same_name_contact.is_deleted = 0
                ) THEN contact.how_we_met
                ELSE ''
            END AS name_qualifier
        FROM contact
        INNER JOIN account_contact
            ON account_contact.contact_id = contact.id AND account_contact.account_id = :accountId
        WHERE contact.is_deleted = 0
            AND EXISTS(
                SELECT 1
                FROM contact_tag
                INNER JOIN scoped_tag
                    ON scoped_tag.tag_id = contact_tag.tag_id
                INNER JOIN account_contact_tag
                    ON account_contact_tag.contact_id = contact_tag.contact_id
                        AND account_contact_tag.tag_id = contact_tag.tag_id
                        AND account_contact_tag.account_id = :accountId
                INNER JOIN tag
                    ON tag.id = contact_tag.tag_id
                INNER JOIN account_tag
                    ON account_tag.tag_id = tag.id AND account_tag.account_id = :accountId
                WHERE contact_tag.contact_id = contact.id
                    AND contact_tag.is_deleted = 0
            )
        ORDER BY
            contact.is_favorite DESC,
            CASE WHEN :sort = 'recently_updated' THEN contact.updated_at END DESC,
            contact.name ASC
        """,
    )
    fun page(
        accountId: Uuid,
        tagId: Uuid,
        scope: String,
        sort: String,
    ): PagingSource<Int, ContactListItemLocalEntity>
}
