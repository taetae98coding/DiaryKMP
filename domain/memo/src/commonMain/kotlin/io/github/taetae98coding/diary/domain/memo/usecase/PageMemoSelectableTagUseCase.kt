package io.github.taetae98coding.diary.domain.memo.usecase

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.flatMapAccount
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.memo.repository.AccountMemoTagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
public class PageMemoSelectableTagUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountMemoTagRepository: AccountMemoTagRepository,
) : FlowUseCase<PageMemoSelectableTagUseCase.Parameter, PagingData<Tag>>() {
    override fun execute(parameter: Parameter): Flow<Result<PagingData<Tag>>> =
        getAccountUseCase.flatMapAccount { account ->
            accountMemoTagRepository
                .pageSelectableTag(
                    account = account,
                    memoId = parameter.memoId,
                    query = parameter.query.trim(),
                ).map { pagingData -> Result.success(pagingData) }
        }

    public data class Parameter(
        val memoId: Uuid,
        val query: String,
    )
}
