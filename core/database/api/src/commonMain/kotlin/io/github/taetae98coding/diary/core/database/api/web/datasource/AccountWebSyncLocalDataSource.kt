package io.github.taetae98coding.diary.core.database.api.web.datasource

import io.github.taetae98coding.diary.core.database.api.web.entity.WebLocalEntity
import kotlin.uuid.Uuid

public interface AccountWebSyncLocalDataSource {
    public suspend fun findPending(accountId: Uuid): List<WebLocalEntity>
}
