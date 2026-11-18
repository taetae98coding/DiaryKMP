package io.github.taetae98coding.diary.domain.dday.usecase

import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.requireAccount
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.dday.repository.AccountDDayRepository
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import org.koin.core.annotation.Factory
import kotlin.time.Clock
import kotlin.uuid.Uuid

@Factory
public class UnsetDDayPrimaryTagUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val requestSyncUseCase: RequestSyncUseCase,
    private val accountDDayRepository: AccountDDayRepository,
    private val clock: Clock,
) : UseCase<Uuid, Unit>() {
    override suspend fun execute(parameter: Uuid) {
        val account = getAccountUseCase.requireAccount()

        accountDDayRepository.updatePrimaryTagId(
            account = account,
            ddayId = parameter,
            primaryTagId = null,
            updatedAt = clock.now(),
        )

        requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)
    }
}
