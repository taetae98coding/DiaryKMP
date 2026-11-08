package io.github.taetae98coding.diary.domain.dday.usecase

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.dday.DDay
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.flatMapAccount
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.dday.repository.AccountTagDDayRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
public class PageTagDDayUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountTagDDayRepository: AccountTagDDayRepository,
) : FlowUseCase<PageTagDDayUseCase.Parameter, PagingData<DDay>>() {
    override fun execute(parameter: Parameter): Flow<Result<PagingData<DDay>>> =
        getAccountUseCase.flatMapAccount { account ->
            accountTagDDayRepository
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
