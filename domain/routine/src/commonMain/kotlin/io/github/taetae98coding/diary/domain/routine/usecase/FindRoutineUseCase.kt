package io.github.taetae98coding.diary.domain.routine.usecase

import io.github.taetae98coding.diary.core.model.routine.Routine
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.flatMapAccount
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.routine.repository.AccountRoutineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
public class FindRoutineUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountRoutineRepository: AccountRoutineRepository,
) : FlowUseCase<Uuid, Routine?>() {
    override fun execute(parameter: Uuid): Flow<Result<Routine?>> =
        getAccountUseCase.flatMapAccount { account ->
            accountRoutineRepository
                .find(
                    account = account,
                    routineId = parameter,
                ).map { routine -> Result.success(routine) }
        }
}
