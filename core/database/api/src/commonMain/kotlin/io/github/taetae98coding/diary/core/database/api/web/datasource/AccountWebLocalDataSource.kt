package io.github.taetae98coding.diary.core.database.api.web.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.entity.WebLocalEntity
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

public interface AccountWebLocalDataSource {
    public fun page(
        accountId: Uuid,
        sort: ListSortLocalEntity,
    ): PagingSource<Int, WebLocalEntity>

    public fun get(
        accountId: Uuid,
        webIdSet: Set<Uuid>,
    ): Flow<List<WebLocalEntity>>

    public fun find(
        accountId: Uuid,
        webId: Uuid,
    ): Flow<WebLocalEntity?>
}
