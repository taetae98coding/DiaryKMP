package io.github.taetae98coding.diary.core.database.api.memotag.datasource

import io.github.taetae98coding.diary.core.database.api.memotag.entity.MemoTagLocalEntity
import kotlin.uuid.Uuid

public interface AccountMemoTagSyncLocalDataSource {
    public suspend fun findPending(accountId: Uuid): List<MemoTagLocalEntity>
}
