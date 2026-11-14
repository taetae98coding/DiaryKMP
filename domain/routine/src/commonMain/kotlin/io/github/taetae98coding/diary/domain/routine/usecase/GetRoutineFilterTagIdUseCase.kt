package io.github.taetae98coding.diary.domain.routine.usecase

import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.flatMapAccount
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.routine.repository.AccountRoutineFilterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
public class GetRoutineFilterTagIdUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountRoutineFilterRepository: AccountRoutineFilterRepository,
) : FlowUseCase<Unit, Set<Uuid>>() {
    override fun execute(parameter: Unit): Flow<Result<Set<Uuid>>> =
        getAccountUseCase.flatMapAccount { account ->
            accountRoutineFilterRepository.getTagIdSet(account = account).map { tagIdSet ->
                Result.success(tagIdSet)
            }
        }
}
