package io.github.taetae98coding.diary.domain.sync.usecase

import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.sync.AccountSyncDataRepository
import io.github.taetae98coding.diary.domain.sync.AccountSyncTimeRepository
import io.github.taetae98coding.diary.domain.sync.SYNC_RESET_THRESHOLD
import org.koin.core.annotation.Factory
import kotlin.time.Clock
import kotlin.uuid.Uuid

@Factory
public class PrepareSyncUseCase internal constructor(
    private val accountSyncTimeRepository: AccountSyncTimeRepository,
    private val accountSyncDataRepository: AccountSyncDataRepository,
    private val clock: Clock,
) : UseCase<Uuid, Unit>() {
    override suspend fun execute(parameter: Uuid) {
        val now = clock.now()
        val syncedAt = accountSyncTimeRepository.find(accountId = parameter) ?: return

        if (now - syncedAt < SYNC_RESET_THRESHOLD) return

        accountSyncDataRepository.delete(accountId = parameter)
        accountSyncTimeRepository.upsert(accountId = parameter, syncedAt = now)
    }
}
