package io.github.taetae98coding.diary.core.database.api.memofilter.datasource

import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

public interface AccountMemoFilterLocalDataSource {
    public fun getTagList(accountId: Uuid): Flow<List<TagLocalEntity>>

    public suspend fun upsert(
        accountId: Uuid,
        tagId: Uuid,
    )

    public suspend fun delete(
        accountId: Uuid,
        tagId: Uuid,
    )

    public suspend fun deleteAll(accountId: Uuid)
}
