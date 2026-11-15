package io.github.taetae98coding.diary.domain.routine.usecase

import io.github.taetae98coding.diary.core.model.routine.Routine
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.requireAccount
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.routine.RoutineInput
import io.github.taetae98coding.diary.domain.routine.exception.RoutineTitleBlankException
import io.github.taetae98coding.diary.domain.routine.repository.AccountRoutineRepository
import io.github.taetae98coding.diary.domain.routine.toDetail
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import org.koin.core.annotation.Factory
import kotlin.time.Clock
import kotlin.uuid.Uuid

@Factory
public class AddRoutineUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val requestSyncUseCase: RequestSyncUseCase,
    private val accountRoutineRepository: AccountRoutineRepository,
    private val clock: Clock,
) : UseCase<AddRoutineUseCase.Parameter, Uuid>() {
    override suspend fun execute(parameter: Parameter): Uuid {
        if (parameter.input.title.isBlank()) throw RoutineTitleBlankException()

        val detail = parameter.input.toDetail()
        val account = getAccountUseCase.requireAccount()
        val now = clock.now()
        val routine =
            Routine(
                id = Uuid.random(),
                detail = detail,
                isFinished = false,
                isDeleted = false,
                updatedAt = now,
                createdAt = now,
            )

        accountRoutineRepository.upsert(
            account = account,
            routine = routine,
            tagIdSet = parameter.tagIdSet,
        )

        requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)

        return routine.id
    }

    public data class Parameter(
        val input: RoutineInput,
        val tagIdSet: Set<Uuid>,
    )
}
