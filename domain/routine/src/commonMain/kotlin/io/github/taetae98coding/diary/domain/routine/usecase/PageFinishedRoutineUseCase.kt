package io.github.taetae98coding.diary.domain.routine.usecase

import androidx.paging.PagingData
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

@Factory
public class PageFinishedRoutineUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountRoutineRepository: AccountRoutineRepository,
) : FlowUseCase<LocalDate, PagingData<RoutineSummary>>() {
    override fun execute(parameter: LocalDate): Flow<Result<PagingData<RoutineSummary>>> =
        getAccountUseCase.flatMapAccount { account ->
            accountRoutineRepository
                .pageFinished(
                    account = account,
                    currentCycle = routineCurrentCycle(today = parameter),
                ).map { pagingData -> Result.success(pagingData) }
        }
}
