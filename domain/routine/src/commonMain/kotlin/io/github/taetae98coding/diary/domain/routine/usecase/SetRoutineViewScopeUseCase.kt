package io.github.taetae98coding.diary.domain.routine.usecase

import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.model.routine.RoutineViewScope
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.requireAccount
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.routine.repository.AccountRoutineFilterRepository

@Inject
public class SetRoutineViewScopeUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountRoutineFilterRepository: AccountRoutineFilterRepository,
) : UseCase<RoutineViewScope, Unit>() {
    override suspend fun execute(parameter: RoutineViewScope) {
        val account = getAccountUseCase.requireAccount()

        accountRoutineFilterRepository.upsertViewScope(
            account = account,
            viewScope = parameter,
        )
    }
}
