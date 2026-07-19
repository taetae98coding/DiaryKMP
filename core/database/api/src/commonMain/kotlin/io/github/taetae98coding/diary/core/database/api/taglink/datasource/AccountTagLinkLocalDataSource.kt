package io.github.taetae98coding.diary.core.database.api.taglink.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

public interface AccountTagLinkLocalDataSource {
    public fun getTagList(
        accountId: Uuid,
        fromTagId: Uuid,
    ): Flow<List<TagLocalEntity>>

    public fun pageSelectableTag(
        accountId: Uuid,
        fromTagId: Uuid,
        query: String,
    ): PagingSource<Int, TagLocalEntity>
}
