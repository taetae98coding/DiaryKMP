package io.github.taetae98coding.diary.domain.sync.usecase

import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.sync.SyncManager
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory

@Factory
public class SchedulePeriodicSyncUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val syncManager: SyncManager,
) : UseCase<Unit, Unit>() {
    override suspend fun execute(parameter: Unit) {
        val account = getAccountUseCase(parameter = Unit).first().getOrThrow()

        if (account is Account.User && account.isSessionValid) {
            syncManager.schedulePeriodicSync(accountId = account.id)
        } else {
            syncManager.cancelPeriodicSync()
        }
    }
}
