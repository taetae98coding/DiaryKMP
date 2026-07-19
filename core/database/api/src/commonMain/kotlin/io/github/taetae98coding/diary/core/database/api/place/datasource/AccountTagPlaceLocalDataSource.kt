package io.github.taetae98coding.diary.core.database.api.place.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagScopeLocalEntity
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

public interface AccountTagPlaceLocalDataSource {
    public fun page(
        accountId: Uuid,
        tagId: Uuid,
        scope: TagScopeLocalEntity,
        sort: ListSortLocalEntity,
    ): PagingSource<Int, PlaceLocalEntity>

    public fun get(
        accountId: Uuid,
        tagId: Uuid,
        scope: TagScopeLocalEntity,
        south: Double,
        north: Double,
        west: Double,
        east: Double,
        sort: ListSortLocalEntity,
    ): Flow<List<PlaceLocalEntity>>
}
