package io.github.taetae98coding.diary.domain.memo.usecase

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.flatMapAccount
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.memo.repository.AccountMemoContactRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
public class PageMemoSelectableContactUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountMemoContactRepository: AccountMemoContactRepository,
) : FlowUseCase<String, PagingData<Contact>>() {
    override fun execute(parameter: String): Flow<Result<PagingData<Contact>>> =
        getAccountUseCase.flatMapAccount { account ->
            accountMemoContactRepository
                .pageSelectableContact(
                    account = account,
                    query = parameter.trim(),
                ).map { pagingData -> Result.success(pagingData) }
        }
}
