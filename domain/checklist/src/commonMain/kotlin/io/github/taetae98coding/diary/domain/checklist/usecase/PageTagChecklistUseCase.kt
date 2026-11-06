package io.github.taetae98coding.diary.domain.checklist.usecase

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.checklist.ChecklistSummary
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.flatMapAccount
import io.github.taetae98coding.diary.domain.checklist.repository.AccountTagChecklistRepository
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
public class PageTagChecklistUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountTagChecklistRepository: AccountTagChecklistRepository,
) : FlowUseCase<PageTagChecklistUseCase.Parameter, PagingData<ChecklistSummary>>() {
    override fun execute(parameter: Parameter): Flow<Result<PagingData<ChecklistSummary>>> =
        getAccountUseCase.flatMapAccount { account ->
            accountTagChecklistRepository
                .page(
                    account = account,
                    tagId = parameter.tagId,
                    scope = parameter.scope,
                ).map { pagingData -> Result.success(pagingData) }
        }

    public data class Parameter(
        val tagId: Uuid,
        val scope: TagScope,
    )
}
