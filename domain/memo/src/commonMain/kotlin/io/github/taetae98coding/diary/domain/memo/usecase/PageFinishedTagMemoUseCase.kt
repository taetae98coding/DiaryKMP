package io.github.taetae98coding.diary.domain.memo.usecase

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.flatMapAccount
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.memo.repository.AccountTagMemoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
public class PageFinishedTagMemoUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountTagMemoRepository: AccountTagMemoRepository,
) : FlowUseCase<PageFinishedTagMemoUseCase.Parameter, PagingData<Memo>>() {
    override fun execute(parameter: Parameter): Flow<Result<PagingData<Memo>>> =
        getAccountUseCase.flatMapAccount { account ->
            accountTagMemoRepository
                .pageFinished(
                    account = account,
                    tagId = parameter.tagId,
                    sort = parameter.sort,
                ).map { pagingData -> Result.success(pagingData) }
        }

    public data class Parameter(
        val tagId: Uuid,
        val sort: ListSort,
    )
}
