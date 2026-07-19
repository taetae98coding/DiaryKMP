package io.github.taetae98coding.diary.core.database.api.memoweb.datasource

import io.github.taetae98coding.diary.core.database.api.memoweb.entity.MemoWebLocalEntity
import kotlin.uuid.Uuid

public interface AccountMemoWebSyncLocalDataSource {
    public suspend fun findPending(accountId: Uuid): List<MemoWebLocalEntity>
}
