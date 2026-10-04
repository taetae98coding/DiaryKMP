package io.github.taetae98coding.diary.data.search.repository

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.database.api.search.datasource.SearchPlaceLocalDataSource
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.data.core.mapper.toDomain
import io.github.taetae98coding.diary.data.core.mapper.toLocal
import io.github.taetae98coding.diary.data.core.paging.pagingFlow
import io.github.taetae98coding.diary.domain.search.repository.AccountSearchPlaceRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

@Factory
internal class AccountSearchPlaceRepositoryImpl(
    private val searchPlaceLocalDataSource: SearchPlaceLocalDataSource,
) : AccountSearchPlaceRepository {
    override fun page(
        account: Account,
        query: String,
        sort: ListSort,
    ): Flow<PagingData<Place>> =
        pagingFlow(
            pagingSourceFactory = {
                searchPlaceLocalDataSource.page(
                    accountId = account.id,
                    query = query,
                    sort = sort.toLocal(),
                )
            },
            transform = { local -> local.toDomain() },
        )
}
