package io.github.taetae98coding.diary.domain.routine.usecase

import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.flatMapAccount
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.routine.repository.AccountRoutineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
public class GetUnfinishedRoutineExistsUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountRoutineRepository: AccountRoutineRepository,
) : FlowUseCase<Unit, Boolean>() {
    override fun execute(parameter: Unit): Flow<Result<Boolean>> =
        getAccountUseCase.flatMapAccount { account ->
            accountRoutineRepository
                .getUnfinishedExists(account = account)
                .map { exists -> Result.success(exists) }
        }
}
