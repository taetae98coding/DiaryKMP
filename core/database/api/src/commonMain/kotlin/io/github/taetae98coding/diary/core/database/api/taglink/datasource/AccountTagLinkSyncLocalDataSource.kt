package io.github.taetae98coding.diary.core.database.api.taglink.datasource

import io.github.taetae98coding.diary.core.database.api.taglink.entity.TagLinkLocalEntity
import kotlin.uuid.Uuid

public interface AccountTagLinkSyncLocalDataSource {
    public suspend fun findPending(accountId: Uuid): List<TagLinkLocalEntity>
}
