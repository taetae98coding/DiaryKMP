package io.github.taetae98coding.diary.data.search.repository

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.database.api.search.datasource.SearchDDayLocalDataSource
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.dday.DDay
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.data.core.mapper.toDomain
import io.github.taetae98coding.diary.data.core.mapper.toLocal
import io.github.taetae98coding.diary.data.core.paging.pagingFlow
import io.github.taetae98coding.diary.domain.search.repository.AccountSearchDDayRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

@Factory
internal class AccountSearchDDayRepositoryImpl(
    private val searchDDayLocalDataSource: SearchDDayLocalDataSource,
) : AccountSearchDDayRepository {
    override fun page(
        account: Account,
        query: String,
        sort: ListSort,
    ): Flow<PagingData<DDay>> =
        pagingFlow(
            pagingSourceFactory = {
                searchDDayLocalDataSource.page(
                    accountId = account.id,
                    query = query,
                    sort = sort.toLocal(),
                )
            },
            transform = { local -> local.toDomain() },
        )
}
