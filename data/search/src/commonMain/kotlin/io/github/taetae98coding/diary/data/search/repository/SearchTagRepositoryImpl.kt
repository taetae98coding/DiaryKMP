package io.github.taetae98coding.diary.data.search.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import io.github.taetae98coding.diary.core.database.api.search.datasource.SearchTagLocalDataSource
import io.github.taetae98coding.diary.core.mapper.list.toLocal
import io.github.taetae98coding.diary.core.mapper.tag.toDomain
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.search.repository.SearchTagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
internal class SearchTagRepositoryImpl(
    private val searchTagLocalDataSource: SearchTagLocalDataSource,
) : SearchTagRepository {
    override fun page(
        account: Account,
        query: String,
        sort: ListSort,
    ): Flow<PagingData<Tag>> =
        Pager(
            config = PagingConfig(pageSize = PAGE_SIZE),
            pagingSourceFactory = {
                searchTagLocalDataSource.page(
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
