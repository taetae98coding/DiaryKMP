package io.github.taetae98coding.diary.core.database.impl.musictag.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Query
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entitytag.dao.AccountEntityTagDao
import io.github.taetae98coding.diary.core.database.impl.musictag.entity.AccountMusicTagLocalEntity
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
internal interface AccountMusicTagDao : AccountEntityTagDao<AccountMusicTagLocalEntity> {
    @Query(
        """
        SELECT tag.*
        FROM tag
        INNER JOIN account_tag
            ON account_tag.tag_id = tag.id AND account_tag.account_id = :accountId
        INNER JOIN music_tag
            ON music_tag.tag_id = tag.id
                AND music_tag.music_id = :entityId
                AND music_tag.is_deleted = 0
        INNER JOIN account_music_tag
            ON account_music_tag.music_id = music_tag.music_id
                AND account_music_tag.tag_id = music_tag.tag_id
                AND account_music_tag.account_id = :accountId
        INNER JOIN music
            ON music.id = music_tag.music_id
        INNER JOIN account_music
            ON account_music.music_id = music.id AND account_music.account_id = :accountId
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
                    FROM music_tag
                    INNER JOIN account_music_tag
                        ON account_music_tag.music_id = music_tag.music_id
                            AND account_music_tag.tag_id = music_tag.tag_id
                            AND account_music_tag.account_id = :accountId
                    INNER JOIN account_music
                        ON account_music.music_id = music_tag.music_id AND account_music.account_id = :accountId
                    WHERE music_tag.tag_id = tag.id
                        AND music_tag.music_id = :entityId
                        AND music_tag.is_deleted = 0
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
        UPDATE account_music_tag
        SET is_dirty = 1
        WHERE account_id = :accountId AND music_id = :entityId AND tag_id = :tagId
        """,
    )
    override suspend fun markPending(
        accountId: Uuid,
        entityId: Uuid,
        tagId: Uuid,
    ): Int
}
