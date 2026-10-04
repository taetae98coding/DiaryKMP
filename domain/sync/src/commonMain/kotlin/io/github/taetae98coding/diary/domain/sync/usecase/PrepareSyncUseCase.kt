package io.github.taetae98coding.diary.domain.sync.usecase

import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.sync.SYNC_RESET_THRESHOLD
import io.github.taetae98coding.diary.domain.sync.repository.AccountSyncDataRepository
import io.github.taetae98coding.diary.domain.sync.repository.AccountSyncTimeRepository
import org.koin.core.annotation.Factory
import kotlin.time.Clock

@Factory
public class PrepareSyncUseCase internal constructor(
    private val accountSyncTimeRepository: AccountSyncTimeRepository,
    private val accountSyncDataRepository: AccountSyncDataRepository,
    private val clock: Clock,
) : UseCase<Account, Unit>() {
    override suspend fun execute(parameter: Account) {
        val now = clock.now()
        val syncedAt = accountSyncTimeRepository.read(account = parameter) ?: return

        if (now - syncedAt < SYNC_RESET_THRESHOLD) return

        accountSyncDataRepository.delete(account = parameter)
        accountSyncTimeRepository.upsert(account = parameter, syncedAt = now)
    }
}
