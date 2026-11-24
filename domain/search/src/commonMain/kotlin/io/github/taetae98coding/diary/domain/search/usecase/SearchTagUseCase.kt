package io.github.taetae98coding.diary.domain.search.usecase

import androidx.paging.PagingData
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.search.repository.AccountSearchTagRepository
import kotlinx.coroutines.flow.Flow

@Inject
public class SearchTagUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountSearchTagRepository: AccountSearchTagRepository,
) : FlowUseCase<SearchTagUseCase.Parameter, PagingData<Tag>>() {
    override fun execute(parameter: Parameter): Flow<Result<PagingData<Tag>>> =
        getAccountUseCase.searchPagingFlow(query = parameter.query) { account, query ->
            accountSearchTagRepository.page(
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
