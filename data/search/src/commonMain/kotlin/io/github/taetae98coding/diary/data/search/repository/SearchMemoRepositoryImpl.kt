package io.github.taetae98coding.diary.data.search.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import io.github.taetae98coding.diary.core.database.api.search.datasource.SearchMemoLocalDataSource
import io.github.taetae98coding.diary.core.mapper.list.toLocal
import io.github.taetae98coding.diary.core.mapper.memo.toDomain
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.domain.search.repository.SearchMemoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
internal class SearchMemoRepositoryImpl(
    private val searchMemoLocalDataSource: SearchMemoLocalDataSource,
) : SearchMemoRepository {
    override fun page(
        account: Account,
        query: String,
        sort: ListSort,
    ): Flow<PagingData<Memo>> =
        Pager(
            config = PagingConfig(pageSize = PAGE_SIZE),
            pagingSourceFactory = {
                searchMemoLocalDataSource.page(
                    accountId = account.id,
                    query = query,
                    sort = sort.toLocal(),
                )
            },
        ).flow.map { pagingData ->
            pagingData.map { local -> local.toDomain() }
        }

    private companion object {
        const val PAGE_SIZE: Int = 20
    }
}
