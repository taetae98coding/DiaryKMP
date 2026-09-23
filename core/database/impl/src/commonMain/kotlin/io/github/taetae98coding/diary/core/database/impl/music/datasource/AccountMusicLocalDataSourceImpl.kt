package io.github.taetae98coding.diary.core.database.impl.music.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.music.datasource.AccountMusicLocalDataSource
import io.github.taetae98coding.diary.core.database.api.music.entity.MusicLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
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
}
