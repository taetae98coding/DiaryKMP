package io.github.taetae98coding.diary.data.search.repository

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.database.api.search.datasource.SearchMemoLocalDataSource
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.data.core.mapper.toDomain
import io.github.taetae98coding.diary.data.core.mapper.toLocal
import io.github.taetae98coding.diary.data.core.paging.pagingFlow
import io.github.taetae98coding.diary.domain.search.repository.AccountSearchMemoRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

@Factory
internal class AccountSearchMemoRepositoryImpl(
    private val searchMemoLocalDataSource: SearchMemoLocalDataSource,
) : AccountSearchMemoRepository {
    override fun page(
        account: Account,
        query: String,
        sort: ListSort,
    ): Flow<PagingData<Memo>> =
        pagingFlow(
            pagingSourceFactory = {
                searchMemoLocalDataSource.page(
                    accountId = account.id,
                    query = query,
                    sort = sort.toLocal(),
                )
            },
            transform = { local -> local.toDomain() },
        )
}
