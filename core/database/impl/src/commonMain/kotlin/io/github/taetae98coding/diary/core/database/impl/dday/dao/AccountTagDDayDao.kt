package io.github.taetae98coding.diary.core.database.impl.dday.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Query
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayLocalEntity
import io.github.taetae98coding.diary.core.database.impl.tag.dao.SCOPED_TAG_CTE
import kotlin.uuid.Uuid

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
internal interface AccountTagDDayDao {
    @Query(
        SCOPED_TAG_CTE +
            """
        SELECT dday.*
        FROM dday
        INNER JOIN account_dday
            ON account_dday.dday_id = dday.id AND account_dday.account_id = :accountId
        WHERE dday.is_deleted = 0
            AND EXISTS(
                SELECT 1
                FROM dday_tag
                INNER JOIN scoped_tag
                    ON scoped_tag.tag_id = dday_tag.tag_id
                INNER JOIN account_dday_tag
                    ON account_dday_tag.dday_id = dday_tag.dday_id
                        AND account_dday_tag.tag_id = dday_tag.tag_id
                        AND account_dday_tag.account_id = :accountId
                INNER JOIN tag
                    ON tag.id = dday_tag.tag_id
                INNER JOIN account_tag
                    ON account_tag.tag_id = tag.id AND account_tag.account_id = :accountId
                WHERE dday_tag.dday_id = dday.id
                    AND dday_tag.is_deleted = 0
            )
        ORDER BY dday.title ASC
        """,
    )
    fun page(
        accountId: Uuid,
        tagId: Uuid,
        scope: String,
    ): PagingSource<Int, DDayLocalEntity>
}
