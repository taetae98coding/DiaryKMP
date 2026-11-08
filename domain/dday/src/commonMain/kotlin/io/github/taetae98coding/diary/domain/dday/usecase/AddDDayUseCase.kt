package io.github.taetae98coding.diary.domain.dday.usecase

import io.github.taetae98coding.diary.core.model.dday.DDay
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.requireAccount
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.dday.DDayInput
import io.github.taetae98coding.diary.domain.dday.exception.DDayTitleBlankException
import io.github.taetae98coding.diary.domain.dday.repository.AccountDDayRepository
import io.github.taetae98coding.diary.domain.dday.toDetail
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import org.koin.core.annotation.Factory
import kotlin.time.Clock
import kotlin.uuid.Uuid

@Factory
public class AddDDayUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val requestSyncUseCase: RequestSyncUseCase,
    private val accountDDayRepository: AccountDDayRepository,
    private val clock: Clock,
) : UseCase<AddDDayUseCase.Parameter, Uuid>() {
    override suspend fun execute(parameter: Parameter): Uuid {
        if (parameter.input.title.isBlank()) throw DDayTitleBlankException()

        val detail = parameter.input.toDetail()
        val account = getAccountUseCase.requireAccount()
        val now = clock.now()
        val dday =
            DDay(
                id = Uuid.random(),
                detail = detail,
                isDeleted = false,
                updatedAt = now,
                createdAt = now,
            )

        accountDDayRepository.upsert(
            account = account,
            dday = dday,
            tagIdSet = parameter.tagIdSet,
        )

        requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)

        return dday.id
    }

    public data class Parameter(
        val input: DDayInput,
        val tagIdSet: Set<Uuid>,
    )
}
