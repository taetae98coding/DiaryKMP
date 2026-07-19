package io.github.taetae98coding.diary.core.database.api.place.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

public interface AccountPlaceLocalDataSource {
    public fun page(
        accountId: Uuid,
        query: String,
        sort: ListSortLocalEntity,
    ): PagingSource<Int, PlaceLocalEntity>

    public fun get(
        accountId: Uuid,
        placeIdSet: Set<Uuid>,
    ): Flow<List<PlaceLocalEntity>>

    public fun get(
        accountId: Uuid,
        south: Double,
        north: Double,
        west: Double,
        east: Double,
        sort: ListSortLocalEntity,
    ): Flow<List<PlaceLocalEntity>>

    public fun find(
        accountId: Uuid,
        placeId: Uuid,
    ): Flow<PlaceLocalEntity?>
}
