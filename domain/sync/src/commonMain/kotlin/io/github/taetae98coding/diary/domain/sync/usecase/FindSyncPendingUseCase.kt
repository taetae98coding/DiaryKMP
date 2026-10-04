package io.github.taetae98coding.diary.domain.sync.usecase

import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.flatMapAccount
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.sync.repository.AccountSyncPendingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
public class FindSyncPendingUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountSyncPendingRepository: AccountSyncPendingRepository,
) : FlowUseCase<Unit, Boolean>() {
    override fun execute(parameter: Unit): Flow<Result<Boolean>> =
        getAccountUseCase.flatMapAccount { account ->
            accountSyncPendingRepository
                .find(account = account)
                .map { hasPending -> Result.success(hasPending) }
        }
}
