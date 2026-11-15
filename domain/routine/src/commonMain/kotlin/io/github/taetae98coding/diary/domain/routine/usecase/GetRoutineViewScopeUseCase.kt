package io.github.taetae98coding.diary.domain.routine.usecase

import io.github.taetae98coding.diary.core.model.routine.RoutineViewScope
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.flatMapAccount
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.routine.repository.AccountRoutineFilterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
public class GetRoutineViewScopeUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountRoutineFilterRepository: AccountRoutineFilterRepository,
) : FlowUseCase<Unit, RoutineViewScope>() {
    override fun execute(parameter: Unit): Flow<Result<RoutineViewScope>> =
        getAccountUseCase.flatMapAccount { account ->
            accountRoutineFilterRepository
                .getViewScope(account = account)
                .map { viewScope -> Result.success(viewScope) }
        }
}
