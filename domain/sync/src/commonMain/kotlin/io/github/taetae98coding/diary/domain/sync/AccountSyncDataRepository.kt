package io.github.taetae98coding.diary.domain.sync

import kotlin.uuid.Uuid

public interface AccountSyncDataRepository {
    public suspend fun delete(accountId: Uuid)
}
