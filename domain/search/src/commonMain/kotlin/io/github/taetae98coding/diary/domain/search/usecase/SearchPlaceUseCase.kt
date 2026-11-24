package io.github.taetae98coding.diary.domain.search.usecase

import androidx.paging.PagingData
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.search.repository.AccountSearchPlaceRepository
import kotlinx.coroutines.flow.Flow

@Inject
public class SearchPlaceUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountSearchPlaceRepository: AccountSearchPlaceRepository,
) : FlowUseCase<SearchPlaceUseCase.Parameter, PagingData<Place>>() {
    override fun execute(parameter: Parameter): Flow<Result<PagingData<Place>>> =
        getAccountUseCase.searchPagingFlow(query = parameter.query) { account, query ->
            accountSearchPlaceRepository.page(
                account = account,
                query = query,
                sort = parameter.sort,
            )
        }

    public data class Parameter(
        val query: String,
        val sort: ListSort,
    )
}
