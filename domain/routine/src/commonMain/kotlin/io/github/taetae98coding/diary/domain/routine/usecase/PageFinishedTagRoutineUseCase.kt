package io.github.taetae98coding.diary.domain.routine.usecase

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.flatMapAccount
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.routine.progress.routineCurrentCycle
import io.github.taetae98coding.diary.domain.routine.repository.AccountTagRoutineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
public class PageFinishedTagRoutineUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountTagRoutineRepository: AccountTagRoutineRepository,
) : FlowUseCase<PageFinishedTagRoutineUseCase.Parameter, PagingData<RoutineSummary>>() {
    override fun execute(parameter: Parameter): Flow<Result<PagingData<RoutineSummary>>> =
        getAccountUseCase.flatMapAccount { account ->
            accountTagRoutineRepository
                .pageFinished(
                    account = account,
                    tagId = parameter.tagId,
                    currentCycle = routineCurrentCycle(today = parameter.today),
                ).map { pagingData -> Result.success(pagingData) }
        }

    public data class Parameter(
        val tagId: Uuid,
        val today: LocalDate,
    )
}
