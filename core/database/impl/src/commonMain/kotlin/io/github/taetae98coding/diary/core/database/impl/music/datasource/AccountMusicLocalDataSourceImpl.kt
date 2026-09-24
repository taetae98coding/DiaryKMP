package io.github.taetae98coding.diary.core.database.impl.music.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.music.datasource.AccountMusicLocalDataSource
import io.github.taetae98coding.diary.core.database.api.music.entity.MusicLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountMusicLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountMusicLocalDataSource {
    override fun page(
        accountId: Uuid,
        sort: ListSortLocalEntity,
    ): PagingSource<Int, MusicLocalEntity> =
        database.accountMusicDao().page(
            accountId = accountId,
            sort = sort.queryValue,
        )

    override suspend fun findList(
        accountId: Uuid,
        sort: ListSortLocalEntity,
    ): List<MusicLocalEntity> =
        database.accountMusicDao().findList(
            accountId = accountId,
            sort = sort.queryValue,
        )

    override fun find(
        accountId: Uuid,
        musicId: Uuid,
    ): Flow<MusicLocalEntity?> =
        database.accountMusicDao().find(
            accountId = accountId,
            musicId = musicId,
        )
}
