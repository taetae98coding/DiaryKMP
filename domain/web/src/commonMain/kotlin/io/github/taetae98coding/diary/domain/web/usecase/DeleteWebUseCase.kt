package io.github.taetae98coding.diary.domain.web.usecase

import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.github.taetae98coding.diary.domain.web.repository.AccountWebRepository
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory
import kotlin.time.Clock
import kotlin.uuid.Uuid

@Factory
public class DeleteWebUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val requestSyncUseCase: RequestSyncUseCase,
    private val accountWebRepository: AccountWebRepository,
    private val clock: Clock,
) : UseCase<Uuid, Int>() {
    override suspend fun execute(parameter: Uuid): Int {
        val account = getAccountUseCase(parameter = Unit).first().getOrThrow()

        val updatedCount =
            accountWebRepository.updateDeleted(
                account = account,
                webId = parameter,
                isDeleted = true,
                updatedAt = clock.now(),
            )

        requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)

        return updatedCount
    }
}
