package io.github.taetae98coding.diary.domain.sync.usecase

import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.sync.AccountSyncDataRepository
import io.github.taetae98coding.diary.domain.sync.AccountSyncTimeRepository
import io.github.taetae98coding.diary.domain.sync.SYNC_RESET_THRESHOLD
import io.github.taetae98coding.diary.domain.sync.SyncManager
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory
import kotlin.time.Clock
import kotlin.uuid.Uuid

@Factory
public class RequestSyncUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountSyncTimeRepository: AccountSyncTimeRepository,
    private val accountSyncDataRepository: AccountSyncDataRepository,
    private val syncManager: SyncManager,
    private val clock: Clock,
) : UseCase<SyncTrigger, Unit>() {
    override suspend fun execute(parameter: SyncTrigger) {
        val account = getAccountUseCase(parameter = Unit).first().getOrThrow()

        if (account !is Account.User) return
        if (!account.isSessionValid) return

        resetStaleData(accountId = account.id)

        syncManager.requestSync(reportsProgress = parameter.reportsProgress())
    }

    private suspend fun resetStaleData(accountId: Uuid) {
        val now = clock.now()
        val syncedAt = accountSyncTimeRepository.find(accountId = accountId) ?: return

        if (now - syncedAt < SYNC_RESET_THRESHOLD) return

        accountSyncDataRepository.delete(accountId = accountId)
        // 지운 직후를 새 기준으로 삼는다. 이어지는 동기화가 실패해도 계기마다 다시 지우지 않는다.
        accountSyncTimeRepository.upsert(accountId = accountId, syncedAt = now)
    }

    private fun SyncTrigger.reportsProgress(): Boolean =
        when (this) {
            SyncTrigger.USER_REQUESTED, SyncTrigger.ACCOUNT_CONFIRMED -> true
            SyncTrigger.DATA_CHANGED -> false
        }
}
