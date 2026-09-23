package io.github.taetae98coding.diary.core.database.api.memocontact.datasource

import io.github.taetae98coding.diary.core.database.api.memocontact.entity.MemoContactLocalEntity
import kotlin.uuid.Uuid

public interface AccountMemoContactSyncLocalDataSource {
    public suspend fun findPending(accountId: Uuid): List<MemoContactLocalEntity>
}
