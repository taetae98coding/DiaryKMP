package io.github.taetae98coding.diary.core.database.api.music.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.music.entity.MusicLocalEntity
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

public interface AccountMusicLocalDataSource {
    public fun page(
        accountId: Uuid,
        sort: ListSortLocalEntity,
    ): PagingSource<Int, MusicLocalEntity>

    public suspend fun findList(
        accountId: Uuid,
        sort: ListSortLocalEntity,
    ): List<MusicLocalEntity>

    public fun find(
        accountId: Uuid,
        musicId: Uuid,
    ): Flow<MusicLocalEntity?>
}
