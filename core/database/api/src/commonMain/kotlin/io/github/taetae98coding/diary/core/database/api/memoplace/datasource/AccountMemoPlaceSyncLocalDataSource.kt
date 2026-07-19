package io.github.taetae98coding.diary.core.database.api.memoplace.datasource

import io.github.taetae98coding.diary.core.database.api.memoplace.entity.MemoPlaceLocalEntity
import kotlin.uuid.Uuid

public interface AccountMemoPlaceSyncLocalDataSource {
    public suspend fun findPending(accountId: Uuid): List<MemoPlaceLocalEntity>
}
