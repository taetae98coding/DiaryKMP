package io.github.taetae98coding.diary.core.database.impl.music.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Query
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import io.github.taetae98coding.diary.core.database.api.music.entity.MusicLocalEntity
import io.github.taetae98coding.diary.core.database.impl.tag.dao.SCOPED_TAG_CTE
import kotlin.uuid.Uuid

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
internal interface AccountTagMusicDao {
    @Query(
        SCOPED_TAG_CTE +
            """
        SELECT music.*
        FROM music
        INNER JOIN account_music
            ON account_music.music_id = music.id AND account_music.account_id = :accountId
        WHERE music.is_deleted = 0
            AND EXISTS(
                SELECT 1
                FROM music_tag
                INNER JOIN scoped_tag
                    ON scoped_tag.tag_id = music_tag.tag_id
                INNER JOIN account_music_tag
                    ON account_music_tag.music_id = music_tag.music_id
                        AND account_music_tag.tag_id = music_tag.tag_id
                        AND account_music_tag.account_id = :accountId
                INNER JOIN tag
                    ON tag.id = music_tag.tag_id
                INNER JOIN account_tag
                    ON account_tag.tag_id = tag.id AND account_tag.account_id = :accountId
                WHERE music_tag.music_id = music.id
                    AND music_tag.is_deleted = 0
            )
        ORDER BY
            CASE WHEN :sort = 'recently_updated' THEN music.updated_at END DESC,
            music.title ASC
        """,
    )
    fun page(
        accountId: Uuid,
        tagId: Uuid,
        scope: String,
        sort: String,
    ): PagingSource<Int, MusicLocalEntity>
}
