package io.github.taetae98coding.diary.domain.tag.usecase

import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.flatMapAccount
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.tag.repository.AccountTagFilterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Inject
public class GetTopLevelTagFilterUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountTagFilterRepository: AccountTagFilterRepository,
) : FlowUseCase<Unit, Boolean>() {
    override fun execute(parameter: Unit): Flow<Result<Boolean>> =
        getAccountUseCase.flatMapAccount { account ->
            accountTagFilterRepository
                .getTopLevelOnly(account = account)
                .map { isTopLevelOnly -> Result.success(isTopLevelOnly) }
        }
}
