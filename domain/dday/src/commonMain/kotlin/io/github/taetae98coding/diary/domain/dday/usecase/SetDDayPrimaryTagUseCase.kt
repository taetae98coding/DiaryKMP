package io.github.taetae98coding.diary.domain.dday.usecase

import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.requireAccount
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.dday.repository.AccountDDayRepository
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import kotlin.time.Clock
import kotlin.uuid.Uuid

@Inject
public class SetDDayPrimaryTagUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val requestSyncUseCase: RequestSyncUseCase,
    private val accountDDayRepository: AccountDDayRepository,
    private val clock: Clock,
) : UseCase<SetDDayPrimaryTagUseCase.Parameter, Unit>() {
    override suspend fun execute(parameter: Parameter) {
        val account = getAccountUseCase.requireAccount()

        accountDDayRepository.updatePrimaryTagId(
            account = account,
            ddayId = parameter.ddayId,
            primaryTagId = parameter.tagId,
            updatedAt = clock.now(),
        )

        requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)
    }

    public data class Parameter(
        val ddayId: Uuid,
        val tagId: Uuid,
    )
}
