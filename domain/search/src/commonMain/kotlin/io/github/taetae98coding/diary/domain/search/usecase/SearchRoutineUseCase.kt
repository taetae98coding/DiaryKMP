package io.github.taetae98coding.diary.domain.search.usecase

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.flatMapAccount
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.routine.progress.routineCurrentCycle
import io.github.taetae98coding.diary.domain.search.repository.AccountSearchRoutineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import org.koin.core.annotation.Factory

@Factory
public class SearchRoutineUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountSearchRoutineRepository: AccountSearchRoutineRepository,
) : FlowUseCase<SearchRoutineUseCase.Parameter, PagingData<RoutineSummary>>() {
    override fun execute(parameter: Parameter): Flow<Result<PagingData<RoutineSummary>>> {
        val query = parameter.query.trim()
        if (query.isEmpty()) return flowOf(Result.success(PagingData.empty()))

        return getAccountUseCase.flatMapAccount { account ->
            accountSearchRoutineRepository
                .page(
                    account = account,
                    query = query,
                    currentCycle = routineCurrentCycle(today = parameter.today),
                ).map { pagingData -> Result.success(pagingData) }
        }
    }

    public data class Parameter(
        val query: String,
        val today: LocalDate,
    )
}
