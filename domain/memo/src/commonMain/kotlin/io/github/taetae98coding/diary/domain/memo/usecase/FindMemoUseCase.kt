package io.github.taetae98coding.diary.domain.memo.usecase

import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.flatMapAccount
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.memo.repository.AccountMemoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
public class FindMemoUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountMemoRepository: AccountMemoRepository,
) : FlowUseCase<Uuid, Memo?>() {
    override fun execute(parameter: Uuid): Flow<Result<Memo?>> =
        getAccountUseCase.flatMapAccount { account ->
            accountMemoRepository
                .find(
                    account = account,
                    memoId = parameter,
                ).map { memo -> Result.success(memo) }
        }
}
