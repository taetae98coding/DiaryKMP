package io.github.taetae98coding.diary.core.database.api.memo.datasource

import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import kotlin.uuid.Uuid

public interface AccountMemoSyncLocalDataSource {
    public suspend fun findPending(accountId: Uuid): List<MemoLocalEntity>
}
