package io.github.taetae98coding.diary.core.database.impl.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Query
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import io.github.taetae98coding.diary.core.database.api.music.entity.MusicLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entity.AccountMusicLocalEntity
import io.github.taetae98coding.diary.library.room3.dao.RoomDao
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
}
