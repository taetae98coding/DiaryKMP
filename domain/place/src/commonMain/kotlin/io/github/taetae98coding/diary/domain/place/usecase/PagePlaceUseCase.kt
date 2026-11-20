package io.github.taetae98coding.diary.domain.place.usecase

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.flatMapAccount
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.place.repository.AccountPlaceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
public class PagePlaceUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountPlaceRepository: AccountPlaceRepository,
) : FlowUseCase<PagePlaceUseCase.Parameter, PagingData<Place>>() {
    override fun execute(parameter: Parameter): Flow<Result<PagingData<Place>>> =
        getAccountUseCase.flatMapAccount { account ->
            accountPlaceRepository
                .page(account = account, query = parameter.query.trim(), sort = parameter.sort)
                .map { pagingData -> Result.success(pagingData) }
        }

    public data class Parameter(
        val query: String,
        val sort: ListSort,
    )
}
