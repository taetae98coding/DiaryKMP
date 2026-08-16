@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.domain.search.usecase

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.search.repository.SearchPlaceRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
public class SearchPlaceUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val searchPlaceRepository: SearchPlaceRepository,
) : FlowUseCase<SearchPlaceUseCase.Parameter, PagingData<Place>>() {
    override fun execute(parameter: Parameter): Flow<Result<PagingData<Place>>> {
        val query = parameter.query.trim()

        if (query.isEmpty()) return flowOf(Result.success(PagingData.empty()))

        return getAccountUseCase(parameter = Unit).flatMapLatest { result ->
            result.fold(
                onSuccess = { account ->
                    searchPlaceRepository
                        .page(
                            account = account,
                            query = query,
                            sort = parameter.sort,
                        ).map { pagingData -> Result.success(pagingData) }
                },
                onFailure = { throwable ->
                    flowOf(Result.failure(throwable))
                },
            )
        }
    }

    public data class Parameter(
        val query: String,
        val sort: ListSort,
    )
}
