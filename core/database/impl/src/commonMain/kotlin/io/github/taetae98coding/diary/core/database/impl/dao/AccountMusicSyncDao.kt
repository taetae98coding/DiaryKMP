package io.github.taetae98coding.diary.core.database.impl.dao

import androidx.room3.Dao
import androidx.room3.Query
import io.github.taetae98coding.diary.core.database.api.music.entity.MusicLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entity.AccountMusicLocalEntity
import io.github.taetae98coding.diary.library.room3.dao.RoomDao
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
internal interface AccountMusicSyncDao : RoomDao<AccountMusicLocalEntity> {
    @Query(
        """
        SELECT music.*
        FROM music
        INNER JOIN account_music
            ON account_music.music_id = music.id AND account_music.account_id = :accountId
        WHERE account_music.is_dirty = 1
        """,
    )
    suspend fun findPending(accountId: Uuid): List<MusicLocalEntity>

    @Query(
        """
        UPDATE account_music
        SET is_dirty = 0
        WHERE account_id = :accountId AND music_id = :musicId
            AND EXISTS(
                SELECT 1
                FROM music
                WHERE music.id = :musicId AND music.updated_at = :updatedAt
            )
        """,
    )
    suspend fun clearPending(
        accountId: Uuid,
        musicId: Uuid,
        updatedAt: Instant,
    ): Int
}
