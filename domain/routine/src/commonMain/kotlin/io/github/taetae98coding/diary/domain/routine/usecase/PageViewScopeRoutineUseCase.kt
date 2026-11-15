@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.domain.routine.usecase

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.routine.RoutineCycle
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.core.model.routine.RoutineViewScope
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.flatMapAccount
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.routine.progress.isDoneOn
import io.github.taetae98coding.diary.domain.routine.progress.routineCurrentCycle
import io.github.taetae98coding.diary.domain.routine.repository.AccountRoutineFilterRepository
import io.github.taetae98coding.diary.domain.routine.repository.AccountRoutineRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import org.koin.core.annotation.Factory

@Factory
public class PageViewScopeRoutineUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountRoutineRepository: AccountRoutineRepository,
    private val accountRoutineFilterRepository: AccountRoutineFilterRepository,
) : FlowUseCase<LocalDate, PagingData<RoutineSummary>>() {
    override fun execute(parameter: LocalDate): Flow<Result<PagingData<RoutineSummary>>> =
        getAccountUseCase.flatMapAccount { account ->
            val currentCycle = routineCurrentCycle(today = parameter)

            accountRoutineFilterRepository
                .getViewScope(account = account)
                .distinctUntilChanged()
                .flatMapLatest { viewScope ->
                    when (viewScope) {
                        RoutineViewScope.TODAY ->
                            accountRoutineRepository.pageStarted(
                                account = account,
                                currentCycle = currentCycle,
                                cycleSet = RoutineCycle.entries.filterTo(mutableSetOf()) { cycle -> cycle.isDoneOn(dayOfWeek = parameter.dayOfWeek) },
                            )

                        RoutineViewScope.ALL ->
                            accountRoutineRepository.page(
                                account = account,
                                currentCycle = currentCycle,
                            )
                    }
                }.map { pagingData -> Result.success(pagingData) }
        }
}
