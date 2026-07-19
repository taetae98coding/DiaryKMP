package io.github.taetae98coding.diary.core.database.api.memotag.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

public interface AccountMemoTagLocalDataSource {
    public fun getTagList(
        accountId: Uuid,
        memoId: Uuid,
    ): Flow<List<TagLocalEntity>>

    public fun pageSelectableTag(
        accountId: Uuid,
        memoId: Uuid,
        query: String,
    ): PagingSource<Int, TagLocalEntity>

    public suspend fun findTagIdList(
        accountId: Uuid,
        memoId: Uuid,
    ): List<Uuid>
}
