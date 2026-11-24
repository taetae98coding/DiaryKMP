package io.github.taetae98coding.diary.domain.search.usecase

import androidx.paging.PagingData
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.routine.progress.routineCurrentCycle
import io.github.taetae98coding.diary.domain.search.repository.AccountSearchRoutineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Inject
public class SearchRoutineUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountSearchRoutineRepository: AccountSearchRoutineRepository,
) : FlowUseCase<SearchRoutineUseCase.Parameter, PagingData<RoutineSummary>>() {
    override fun execute(parameter: Parameter): Flow<Result<PagingData<RoutineSummary>>> =
        getAccountUseCase.searchPagingFlow(query = parameter.query) { account, query ->
            accountSearchRoutineRepository.page(
                account = account,
                query = query,
                currentCycle = routineCurrentCycle(today = parameter.today),
            )
        }

    public data class Parameter(
        val query: String,
        val today: LocalDate,
    )
}
