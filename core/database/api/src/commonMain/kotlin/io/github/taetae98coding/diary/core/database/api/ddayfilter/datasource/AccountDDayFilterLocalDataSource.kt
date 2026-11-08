package io.github.taetae98coding.diary.core.database.api.ddayfilter.datasource

import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

public interface AccountDDayFilterLocalDataSource {
    public fun getTagList(accountId: Uuid): Flow<List<TagLocalEntity>>

    public fun getTagIdList(accountId: Uuid): Flow<List<Uuid>>

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
