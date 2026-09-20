package io.github.taetae98coding.diary.domain.sync.repository

import io.github.taetae98coding.diary.core.model.account.Account
import kotlinx.coroutines.flow.Flow

public interface AccountSyncPendingRepository {
    public fun find(account: Account): Flow<Boolean>
}
