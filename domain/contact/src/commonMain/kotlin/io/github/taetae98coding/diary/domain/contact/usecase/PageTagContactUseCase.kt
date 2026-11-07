package io.github.taetae98coding.diary.domain.contact.usecase

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.contact.ContactListItem
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.flatMapAccount
import io.github.taetae98coding.diary.domain.contact.repository.AccountTagContactRepository
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
public class PageTagContactUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountTagContactRepository: AccountTagContactRepository,
) : FlowUseCase<PageTagContactUseCase.Parameter, PagingData<ContactListItem>>() {
    override fun execute(parameter: Parameter): Flow<Result<PagingData<ContactListItem>>> =
        getAccountUseCase.flatMapAccount { account ->
            accountTagContactRepository
                .page(
                    account = account,
                    tagId = parameter.tagId,
                    scope = parameter.scope,
                    sort = parameter.sort,
                ).map { pagingData -> Result.success(pagingData) }
        }

    public data class Parameter(
        val tagId: Uuid,
        val scope: TagScope,
        val sort: ListSort,
    )
}
