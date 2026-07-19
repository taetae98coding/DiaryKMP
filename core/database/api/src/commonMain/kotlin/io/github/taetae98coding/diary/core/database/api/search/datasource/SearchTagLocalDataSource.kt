package io.github.taetae98coding.diary.core.database.api.search.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import kotlin.uuid.Uuid

public interface SearchTagLocalDataSource {
    public fun page(
        accountId: Uuid,
        query: String,
        sort: ListSortLocalEntity,
    ): PagingSource<Int, TagLocalEntity>
}
