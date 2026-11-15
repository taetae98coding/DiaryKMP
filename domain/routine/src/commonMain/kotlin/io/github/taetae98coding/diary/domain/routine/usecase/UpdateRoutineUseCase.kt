package io.github.taetae98coding.diary.domain.routine.usecase

import io.github.taetae98coding.diary.core.model.routine.RoutineDetail
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.requireAccount
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.routine.RoutineInput
import io.github.taetae98coding.diary.domain.routine.repository.AccountRoutineRepository
import io.github.taetae98coding.diary.domain.routine.toDetail
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory
import kotlin.time.Clock
import kotlin.uuid.Uuid

@Factory
public class UpdateRoutineUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val requestSyncUseCase: RequestSyncUseCase,
    private val findRoutineUseCase: FindRoutineUseCase,
    private val accountRoutineRepository: AccountRoutineRepository,
    private val clock: Clock,
) : UseCase<UpdateRoutineUseCase.Parameter, Int>() {
    override suspend fun execute(parameter: Parameter): Int {
        val inputDetail = parameter.input.toDetail()
        val account = getAccountUseCase.requireAccount()
        val detail = inputDetail.withStoredTitleForBlank(id = parameter.id)

        val updatedCount =
            accountRoutineRepository.updateDetail(
                account = account,
                routineId = parameter.id,
                detail = detail,
                updatedAt = clock.now(),
            )

        requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)

        return updatedCount
    }

    private suspend fun RoutineDetail.withStoredTitleForBlank(id: Uuid): RoutineDetail {
        if (title.isNotBlank()) return this

        val stored =
            findRoutineUseCase(parameter = id)
                .first()
                .getOrThrow()
                ?.detail

        return copy(title = stored?.title.orEmpty())
    }

    public data class Parameter(
        val id: Uuid,
        val input: RoutineInput,
    )
}
