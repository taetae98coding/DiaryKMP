package io.github.taetae98coding.diary.core.database.impl.musictag.dao

import androidx.room3.Dao
import androidx.room3.Query
import io.github.taetae98coding.diary.core.database.impl.entitytag.dao.AccountEntityTagSyncDao
import io.github.taetae98coding.diary.core.database.impl.musictag.entity.AccountMusicTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.musictag.entity.MusicTagLocalEntity
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
internal interface AccountMusicTagSyncDao : AccountEntityTagSyncDao<MusicTagLocalEntity, AccountMusicTagLocalEntity> {
    @Query(
        """
        SELECT music_tag.*
        FROM music_tag
        INNER JOIN account_music_tag
            ON account_music_tag.music_id = music_tag.music_id
                AND account_music_tag.tag_id = music_tag.tag_id
                AND account_music_tag.account_id = :accountId
        WHERE account_music_tag.is_dirty = 1
        """,
    )
    override suspend fun findPending(accountId: Uuid): List<MusicTagLocalEntity>

    @Query(
        """
        UPDATE account_music_tag
        SET is_dirty = 0
        WHERE account_id = :accountId AND music_id = :entityId AND tag_id = :tagId
            AND EXISTS(
                SELECT 1
                FROM music_tag
                WHERE music_tag.music_id = :entityId AND music_tag.tag_id = :tagId AND music_tag.updated_at = :updatedAt
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
