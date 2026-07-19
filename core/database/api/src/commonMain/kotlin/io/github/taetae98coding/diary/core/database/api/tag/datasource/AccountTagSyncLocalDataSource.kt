package io.github.taetae98coding.diary.core.database.api.tag.datasource

import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import kotlin.uuid.Uuid

public interface AccountTagSyncLocalDataSource {
    public suspend fun findPending(accountId: Uuid): List<TagLocalEntity>
}
