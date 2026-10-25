package io.github.taetae98coding.diary.domain.contact.usecase

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.core.model.contact.ContactFilter
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.flatMapAccount
import io.github.taetae98coding.diary.domain.contact.repository.AccountContactRepository
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
public class PageContactUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountContactRepository: AccountContactRepository,
) : FlowUseCase<PageContactUseCase.Parameter, PagingData<Contact>>() {
    override fun execute(parameter: Parameter): Flow<Result<PagingData<Contact>>> =
        getAccountUseCase.flatMapAccount { account ->
            accountContactRepository
                .page(
                    account = account,
                    sort = parameter.sort,
                    filter = parameter.filter,
                ).map { pagingData -> Result.success(pagingData) }
        }

    public data class Parameter(
        val sort: ListSort,
        val filter: ContactFilter,
    )
}
