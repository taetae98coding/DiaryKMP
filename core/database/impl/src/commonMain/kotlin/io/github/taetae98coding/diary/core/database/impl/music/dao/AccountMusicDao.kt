package io.github.taetae98coding.diary.core.database.impl.music.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Query
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import io.github.taetae98coding.diary.core.database.api.music.entity.MusicLocalEntity
import io.github.taetae98coding.diary.core.database.impl.music.entity.AccountMusicLocalEntity
import io.github.taetae98coding.diary.library.room3.dao.RoomDao
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
internal interface AccountMusicDao : RoomDao<AccountMusicLocalEntity> {
    @Query(
        """
        SELECT music.*
        FROM music
        INNER JOIN account_music
            ON account_music.music_id = music.id AND account_music.account_id = :accountId
        WHERE music.is_deleted = 0
        ORDER BY
            CASE WHEN :sort = 'recently_updated' THEN music.updated_at END DESC,
            music.title ASC
        """,
    )
    fun page(
        accountId: Uuid,
        sort: String,
    ): PagingSource<Int, MusicLocalEntity>

    @Query(
        """
        SELECT music.*
        FROM music
        INNER JOIN account_music
            ON account_music.music_id = music.id AND account_music.account_id = :accountId
        WHERE music.is_deleted = 0
        ORDER BY
            CASE WHEN :sort = 'recently_updated' THEN music.updated_at END DESC,
            music.title ASC
        """,
    )
    suspend fun findList(
        accountId: Uuid,
        sort: String,
    ): List<MusicLocalEntity>

    @Query(
        """
        SELECT music.*
        FROM music
        INNER JOIN account_music
            ON account_music.music_id = music.id AND account_music.account_id = :accountId
        WHERE music.id = :musicId
        """,
    )
    fun find(
        accountId: Uuid,
        musicId: Uuid,
    ): Flow<MusicLocalEntity?>

    @Query(
        """
        UPDATE music
        SET link = :link, title = :title, artist = :artist, updated_at = :updatedAt
        WHERE id = :musicId
            AND EXISTS(
                SELECT 1
                FROM account_music
                WHERE account_music.music_id = music.id AND account_music.account_id = :accountId
            )
        """,
    )
    suspend fun updateDetail(
        accountId: Uuid,
        musicId: Uuid,
        link: String,
        title: String,
        artist: String,
        updatedAt: Instant,
    ): Int

    @Query(
        """
        UPDATE music
        SET is_deleted = :isDeleted, updated_at = :updatedAt
        WHERE id = :musicId
            AND EXISTS(
                SELECT 1
                FROM account_music
                WHERE account_music.music_id = music.id AND account_music.account_id = :accountId
            )
        """,
    )
    suspend fun updateDeleted(
        accountId: Uuid,
        musicId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int

    @Query(
        """
        UPDATE account_music
        SET is_dirty = 1
        WHERE account_id = :accountId AND music_id = :musicId
        """,
    )
    suspend fun markPending(
        accountId: Uuid,
        musicId: Uuid,
    ): Int
}
