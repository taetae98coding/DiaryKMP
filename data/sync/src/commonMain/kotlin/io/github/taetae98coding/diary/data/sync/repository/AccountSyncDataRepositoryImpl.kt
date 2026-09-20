package io.github.taetae98coding.diary.data.sync.repository

import io.github.taetae98coding.diary.core.database.api.sync.transaction.AccountDataTransaction
import io.github.taetae98coding.diary.domain.sync.AccountSyncDataRepository
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountSyncDataRepositoryImpl(
    private val accountDataTransaction: AccountDataTransaction,
) : AccountSyncDataRepository {
    override suspend fun delete(accountId: Uuid) {
        accountDataTransaction.delete(accountId = accountId)
    }
}
