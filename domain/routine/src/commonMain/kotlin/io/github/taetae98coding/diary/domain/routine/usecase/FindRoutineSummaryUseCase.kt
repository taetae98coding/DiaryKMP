package io.github.taetae98coding.diary.domain.routine.usecase

import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.flatMapAccount
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.routine.progress.routineCurrentCycle
import io.github.taetae98coding.diary.domain.routine.repository.AccountRoutineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
public class FindRoutineSummaryUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountRoutineRepository: AccountRoutineRepository,
) : FlowUseCase<FindRoutineSummaryUseCase.Parameter, RoutineSummary?>() {
    override fun execute(parameter: Parameter): Flow<Result<RoutineSummary?>> =
        getAccountUseCase.flatMapAccount { account ->
            accountRoutineRepository
                .findSummary(
                    account = account,
                    routineId = parameter.routineId,
                    currentCycle = routineCurrentCycle(today = parameter.today),
                ).map { summary -> Result.success(summary) }
        }

    public data class Parameter(
        val routineId: Uuid,
        val today: LocalDate,
    )
}
