package io.github.taetae98coding.diary.data.playlist.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import io.github.taetae98coding.diary.core.database.api.music.datasource.AccountMusicLocalDataSource
import io.github.taetae98coding.diary.core.database.api.music.transaction.AccountMusicTransaction
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.playlist.Music
import io.github.taetae98coding.diary.data.core.mapper.toLocal
import io.github.taetae98coding.diary.data.playlist.mapper.toDomain
import io.github.taetae98coding.diary.data.playlist.mapper.toLocal
import io.github.taetae98coding.diary.domain.playlist.repository.AccountMusicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
internal class AccountMusicRepositoryImpl(
    private val accountMusicLocalDataSource: AccountMusicLocalDataSource,
    private val accountMusicTransaction: AccountMusicTransaction,
) : AccountMusicRepository {
    override fun page(
        account: Account,
        sort: ListSort,
    ): Flow<PagingData<Music>> =
        Pager(
            config = PagingConfig(pageSize = PAGE_SIZE),
            pagingSourceFactory = {
                accountMusicLocalDataSource.page(
                    accountId = account.id,
                    sort = sort.toLocal(),
                )
            },
        ).flow.map { pagingData ->
            pagingData.map { local -> local.toDomain() }
        }

    override suspend fun upsert(
        account: Account,
        music: Music,
    ) {
        accountMusicTransaction.upsert(
            accountId = account.id,
            musicList = listOf(music.toLocal()),
        )
    }

    private companion object {
        const val PAGE_SIZE: Int = 20
    }
}
