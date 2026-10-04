package io.github.taetae98coding.diary.domain.sync.repository

import io.github.taetae98coding.diary.core.model.account.Account
import kotlin.time.Instant

public interface AccountSyncTimeRepository {
    public suspend fun read(account: Account): Instant?

    public suspend fun upsert(
        account: Account,
        syncedAt: Instant,
    )
}
