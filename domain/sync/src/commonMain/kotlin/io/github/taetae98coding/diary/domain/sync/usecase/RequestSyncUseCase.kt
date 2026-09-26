package io.github.taetae98coding.diary.domain.sync.usecase

import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.sync.SyncManager
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory

@Factory
public class RequestSyncUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val syncManager: SyncManager,
) : UseCase<SyncTrigger, Unit>() {
    override suspend fun execute(parameter: SyncTrigger) {
        val account = getAccountUseCase(parameter = Unit).first().getOrThrow()

        if (account !is Account.User) return
        if (!account.isSessionValid) return

        syncManager.requestSync(reportsProgress = parameter.reportsProgress())
    }

    private fun SyncTrigger.reportsProgress(): Boolean =
        when (this) {
            SyncTrigger.USER_REQUESTED, SyncTrigger.ACCOUNT_CONFIRMED -> true
            SyncTrigger.ACCOUNT_UPDATED, SyncTrigger.DATA_CHANGED -> false
        }
}
