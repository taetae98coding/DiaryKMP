package io.github.taetae98coding.diary.domain.routine.usecase

import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.requireAccount
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.routine.repository.AccountRoutineFilterRepository
import org.koin.core.annotation.Factory

@Factory
public class UnselectAllRoutineFilterTagUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountRoutineFilterRepository: AccountRoutineFilterRepository,
) : UseCase<Unit, Unit>() {
    override suspend fun execute(parameter: Unit) {
        val account = getAccountUseCase.requireAccount()

        accountRoutineFilterRepository.deleteAll(account = account)
    }
}
