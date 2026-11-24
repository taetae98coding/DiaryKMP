package io.github.taetae98coding.diary.data.sync.repository

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.database.api.sync.transaction.AccountDataTransaction
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.sync.repository.AccountSyncDataRepository

@Inject
@ContributesBinding(AppScope::class)
internal class AccountSyncDataRepositoryImpl(
    private val accountDataTransaction: AccountDataTransaction,
) : AccountSyncDataRepository {
    override suspend fun delete(account: Account) {
        accountDataTransaction.delete(accountId = account.id)
    }
}
