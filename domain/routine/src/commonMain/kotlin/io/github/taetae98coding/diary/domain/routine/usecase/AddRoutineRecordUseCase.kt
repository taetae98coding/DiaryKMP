package io.github.taetae98coding.diary.domain.routine.usecase

import io.github.taetae98coding.diary.core.model.routine.RoutineDetail
import io.github.taetae98coding.diary.core.model.routine.RoutineRecord
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.requireAccount
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.routine.ROUTINE_AMOUNT_RANGE
import io.github.taetae98coding.diary.domain.routine.exception.RoutineNotFoundException
import io.github.taetae98coding.diary.domain.routine.exception.RoutineRecordAmountInvalidException
import io.github.taetae98coding.diary.domain.routine.exception.RoutineRecordDateOutOfRangeException
import io.github.taetae98coding.diary.domain.routine.progress.canRecord
import io.github.taetae98coding.diary.domain.routine.repository.AccountRoutineRecordRepository
import io.github.taetae98coding.diary.domain.routine.repository.AccountRoutineRepository
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import org.koin.core.annotation.Factory
import kotlin.time.Clock
import kotlin.uuid.Uuid

@Factory
public class AddRoutineRecordUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val requestSyncUseCase: RequestSyncUseCase,
    private val accountRoutineRepository: AccountRoutineRepository,
    private val accountRoutineRecordRepository: AccountRoutineRecordRepository,
    private val clock: Clock,
) : UseCase<AddRoutineRecordUseCase.Parameter, Uuid>() {
    override suspend fun execute(parameter: Parameter): Uuid {
        val account = getAccountUseCase.requireAccount()
        val routine =
            accountRoutineRepository
                .find(account = account, routineId = parameter.routineId)
                .first()
                ?: throw RoutineNotFoundException()

        invalidException(detail = routine.detail, date = parameter.date, amount = parameter.amount)?.let { exception -> throw exception }

        val now = clock.now()
        val record =
            RoutineRecord(
                id = Uuid.random(),
                routineId = routine.id,
                date = parameter.date,
                amount = parameter.amount,
                isDeleted = false,
                updatedAt = now,
                createdAt = now,
            )

        val addedCount = accountRoutineRecordRepository.upsert(account = account, record = record)
        check(addedCount > 0) { "Routine is not linked: ${routine.id}" }

        requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)

        return record.id
    }

    private fun invalidException(
        detail: RoutineDetail,
        date: LocalDate,
        amount: Int,
    ): Exception? =
        when {
            amount !in ROUTINE_AMOUNT_RANGE -> RoutineRecordAmountInvalidException()
            !detail.canRecord(date = date) -> RoutineRecordDateOutOfRangeException()
            else -> null
        }

    public data class Parameter(
        val routineId: Uuid,
        val date: LocalDate,
        val amount: Int,
    )
}
