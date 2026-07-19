package io.github.taetae98coding.diary.core.database.api.webtag.datasource

import io.github.taetae98coding.diary.core.database.api.webtag.entity.WebTagLocalEntity
import kotlin.uuid.Uuid

public interface AccountWebTagSyncLocalDataSource {
    public suspend fun findPending(accountId: Uuid): List<WebTagLocalEntity>
}
