package io.github.taetae98coding.diary.data.playlist.repository

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.database.api.music.datasource.AccountTagMusicLocalDataSource
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.playlist.Music
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.data.core.mapper.toLocal
import io.github.taetae98coding.diary.data.core.paging.pagingFlow
import io.github.taetae98coding.diary.data.playlist.mapper.toDomain
import io.github.taetae98coding.diary.domain.playlist.repository.AccountTagMusicRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountTagMusicRepositoryImpl(
    private val accountTagMusicLocalDataSource: AccountTagMusicLocalDataSource,
) : AccountTagMusicRepository {
    override fun page(
        account: Account,
        tagId: Uuid,
        scope: TagScope,
        sort: ListSort,
    ): Flow<PagingData<Music>> =
        pagingFlow(
            pagingSourceFactory = {
                accountTagMusicLocalDataSource.page(
                    accountId = account.id,
                    tagId = tagId,
                    scope = scope.toLocal(),
                    sort = sort.toLocal(),
                )
            },
            transform = { local -> local.toDomain() },
        )
}
