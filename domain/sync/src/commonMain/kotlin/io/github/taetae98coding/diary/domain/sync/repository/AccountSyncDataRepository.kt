package io.github.taetae98coding.diary.domain.sync.repository

import io.github.taetae98coding.diary.core.model.account.Account

public interface AccountSyncDataRepository {
    public suspend fun delete(account: Account)
}
