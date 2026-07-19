package io.github.taetae98coding.diary.core.database.api.search.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.entity.WebLocalEntity
import kotlin.uuid.Uuid

public interface SearchWebLocalDataSource {
    public fun page(
        accountId: Uuid,
        query: String,
        sort: ListSortLocalEntity,
    ): PagingSource<Int, WebLocalEntity>
}
