package io.github.taetae98coding.diary.domain.routine.usecase

import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.routine.Routine
import io.github.taetae98coding.diary.core.model.routine.RoutineRecord
import io.github.taetae98coding.diary.core.testing.account.accountUser
import io.github.taetae98coding.diary.core.testing.routine.routine
import io.github.taetae98coding.diary.core.testing.routine.routineDetail
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.routine.repository.AccountRoutineRecordRepository
import io.github.taetae98coding.diary.domain.routine.repository.AccountRoutineRepository
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDate
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlin.time.Clock
import kotlin.time.Instant

private const val MAX_MARGIN_DAY_COUNT = 30

// 기록을 넣고 빼는 UseCase가 함께 쓰는 협력 객체다. 저장된 루틴은 stored로 바꿔 끼운다.
internal class RoutineRecordContext(
    accountResult: Result<Account> = Result.success(Arb.accountUser().next()),
    val today: LocalDate = Arb.kotlinxLocalDate().next(),
    routine: Routine? = inProgressRoutine(today = today),
) {
    val account: Account = accountResult.getOrNull() ?: Account.Guest
    val now: Instant = Arb.secondPrecisionInstant().next()
    val stored: MutableStateFlow<Routine?> = MutableStateFlow(routine)
    val savedList: MutableList<RoutineRecord> = mutableListOf()

    val routineRepository: AccountRoutineRepository =
        mockk<AccountRoutineRepository>().also { repository ->
            every { repository.find(account = any(), routineId = any()) } answers { flowOf(stored.value) }
        }

    val recordRepository: AccountRoutineRecordRepository =
        mockk<AccountRoutineRecordRepository>().also { repository ->
            coEvery { repository.upsert(account = any(), record = any()) } answers {
                savedList += secondArg<RoutineRecord>()
                1
            }
            coEvery { repository.updateDeleted(account = any(), recordId = any(), isDeleted = any(), updatedAt = any()) } returns 1
        }

    val requestSyncUseCase: RequestSyncUseCase =
        mockk<RequestSyncUseCase>().also { useCase ->
            coEvery { useCase(parameter = any()) } returns Result.success(Unit)
        }

    private val getAccountUseCase: GetAccountUseCase =
        mockk<GetAccountUseCase>().also { useCase ->
            every { useCase() } returns flowOf(accountResult)
        }

    private val clock: Clock =
        mockk<Clock>().also { clock ->
            every { clock.now() } returns now
        }

    val addUseCase: AddRoutineRecordUseCase =
        AddRoutineRecordUseCase(
            getAccountUseCase = getAccountUseCase,
            requestSyncUseCase = requestSyncUseCase,
            accountRoutineRepository = routineRepository,
            accountRoutineRecordRepository = recordRepository,
            clock = clock,
        )

    val addQuickUseCase: AddQuickRoutineRecordUseCase =
        AddQuickRoutineRecordUseCase(
            findRoutineUseCase = FindRoutineUseCase(getAccountUseCase = getAccountUseCase, accountRoutineRepository = routineRepository),
            addRoutineRecordUseCase = addUseCase,
        )

    val deleteUseCase: DeleteRoutineRecordUseCase =
        DeleteRoutineRecordUseCase(
            getAccountUseCase = getAccountUseCase,
            requestSyncUseCase = requestSyncUseCase,
            accountRoutineRecordRepository = recordRepository,
            clock = clock,
        )

    fun verifyNothingSaved() {
        coVerify(exactly = 0) { recordRepository.upsert(account = any(), record = any()) }
        coVerify(exactly = 0) { requestSyncUseCase(parameter = any()) }
    }

    // 기록을 넣거나 빼는 동작은 루틴을 저장하는 연산을 부르지 않는다.
    fun verifyRoutineUntouched() {
        coVerify(exactly = 0) { routineRepository.upsert(account = any(), routine = any(), tagIdSet = any()) }
        coVerify(exactly = 0) { routineRepository.updateDetail(account = any(), routineId = any(), detail = any(), updatedAt = any()) }
        coVerify(exactly = 0) { routineRepository.updateFinished(account = any(), routineId = any(), isFinished = any(), updatedAt = any()) }
        coVerify(exactly = 0) { routineRepository.updateDeleted(account = any(), routineId = any(), isDeleted = any(), updatedAt = any()) }
    }
}

// 시작 날짜가 오늘보다 하루 이상 앞인 진행 중인 루틴이다.
internal fun inProgressRoutine(
    today: LocalDate,
    amountPerRecord: Int? = null,
    isDeleted: Boolean = false,
    isFinished: Boolean = false,
): Routine {
    val detail = Arb.routineDetail(startDate = today.minus(Arb.int(1..MAX_MARGIN_DAY_COUNT).next(), DateTimeUnit.DAY)).next()

    return Arb.routine(isDeleted = isDeleted, detail = detail.copy(amountPerRecord = amountPerRecord ?: detail.amountPerRecord), isFinished = isFinished).next()
}
