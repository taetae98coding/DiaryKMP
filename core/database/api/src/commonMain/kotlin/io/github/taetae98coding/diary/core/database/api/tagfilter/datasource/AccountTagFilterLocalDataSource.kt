package io.github.taetae98coding.diary.core.database.api.tagfilter.datasource

import io.github.taetae98coding.diary.core.database.api.tagfilter.entity.TagFilterLocalEntity
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

public interface AccountTagFilterLocalDataSource {
    public fun find(accountId: Uuid): Flow<TagFilterLocalEntity?>

    public suspend fun upsert(
        accountId: Uuid,
        isTopLevelOnly: Boolean,
    )
}
