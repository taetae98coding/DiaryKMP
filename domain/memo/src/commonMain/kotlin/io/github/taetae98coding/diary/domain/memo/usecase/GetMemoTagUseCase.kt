package io.github.taetae98coding.diary.domain.memo.usecase

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
public class GetMemoTagUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountMemoTagRepository: AccountMemoTagRepository,
) : FlowUseCase<Uuid, List<Tag>>() {
    override fun execute(parameter: Uuid): Flow<Result<List<Tag>>> =
        getAccountUseCase.flatMapAccount { account ->
            accountMemoTagRepository
                .getTagList(
                    account = account,
                    memoId = parameter,
                ).map { tagList -> Result.success(tagList) }
        }
}
