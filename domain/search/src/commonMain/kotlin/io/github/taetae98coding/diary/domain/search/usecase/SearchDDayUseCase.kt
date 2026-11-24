package io.github.taetae98coding.diary.domain.search.usecase

import androidx.paging.PagingData
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.model.dday.DDay
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.search.repository.AccountSearchDDayRepository
import kotlinx.coroutines.flow.Flow

@Inject
public class SearchDDayUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountSearchDDayRepository: AccountSearchDDayRepository,
) : FlowUseCase<SearchDDayUseCase.Parameter, PagingData<DDay>>() {
    override fun execute(parameter: Parameter): Flow<Result<PagingData<DDay>>> =
        getAccountUseCase.searchPagingFlow(query = parameter.query) { account, query ->
            accountSearchDDayRepository.page(
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
