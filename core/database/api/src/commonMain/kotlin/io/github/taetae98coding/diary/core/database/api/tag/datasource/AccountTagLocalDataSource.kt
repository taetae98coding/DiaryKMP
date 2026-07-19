package io.github.taetae98coding.diary.core.database.api.tag.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

public interface AccountTagLocalDataSource {
    public fun get(
        accountId: Uuid,
        tagIdSet: Set<Uuid>,
    ): Flow<List<TagLocalEntity>>

    public fun page(
        accountId: Uuid,
        query: String,
        sort: ListSortLocalEntity,
    ): PagingSource<Int, TagLocalEntity>

    public fun pageTopLevel(
        accountId: Uuid,
        sort: ListSortLocalEntity,
    ): PagingSource<Int, TagLocalEntity>

    public fun pageFinished(
        accountId: Uuid,
        sort: ListSortLocalEntity,
    ): PagingSource<Int, TagLocalEntity>

    public fun find(
        accountId: Uuid,
        tagId: Uuid,
    ): Flow<TagLocalEntity?>
}
