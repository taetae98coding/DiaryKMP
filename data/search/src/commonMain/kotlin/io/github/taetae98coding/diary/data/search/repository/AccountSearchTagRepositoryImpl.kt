package io.github.taetae98coding.diary.data.search.repository

import androidx.paging.PagingData
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.database.api.search.datasource.SearchTagLocalDataSource
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.data.core.mapper.toDomain
import io.github.taetae98coding.diary.data.core.mapper.toLocal
import io.github.taetae98coding.diary.data.core.paging.pagingFlow
import io.github.taetae98coding.diary.domain.search.repository.AccountSearchTagRepository
import kotlinx.coroutines.flow.Flow

@Inject
@ContributesBinding(AppScope::class)
internal class AccountSearchTagRepositoryImpl(
    private val searchTagLocalDataSource: SearchTagLocalDataSource,
) : AccountSearchTagRepository {
    override fun page(
        account: Account,
        query: String,
        sort: ListSort,
    ): Flow<PagingData<Tag>> =
        pagingFlow(
            pagingSourceFactory = {
                searchTagLocalDataSource.page(
                    accountId = account.id,
                    query = query,
                    sort = sort.toLocal(),
                )
            },
            transform = { local -> local.toDomain() },
        )
}
