package io.github.taetae98coding.diary.domain.search.usecase

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.search.repository.AccountSearchWebRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

@Factory
public class SearchWebUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountSearchWebRepository: AccountSearchWebRepository,
) : FlowUseCase<SearchWebUseCase.Parameter, PagingData<Web>>() {
    override fun execute(parameter: Parameter): Flow<Result<PagingData<Web>>> =
        getAccountUseCase.searchPagingFlow(query = parameter.query) { account, query ->
            accountSearchWebRepository.page(
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
