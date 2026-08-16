package io.github.taetae98coding.diary.data.search.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import io.github.taetae98coding.diary.core.database.api.search.datasource.SearchWebLocalDataSource
import io.github.taetae98coding.diary.core.mapper.list.toLocal
import io.github.taetae98coding.diary.core.mapper.web.toDomain
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.domain.search.repository.SearchWebRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
internal class SearchWebRepositoryImpl(
    private val searchWebLocalDataSource: SearchWebLocalDataSource,
) : SearchWebRepository {
    override fun page(
        account: Account,
        query: String,
        sort: ListSort,
    ): Flow<PagingData<Web>> =
        Pager(
            config = PagingConfig(pageSize = PAGE_SIZE),
            pagingSourceFactory = {
                searchWebLocalDataSource.page(
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
