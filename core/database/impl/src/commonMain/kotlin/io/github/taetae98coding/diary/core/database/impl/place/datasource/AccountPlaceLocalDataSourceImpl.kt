package io.github.taetae98coding.diary.core.database.impl.place.datasource

import androidx.paging.PagingSource
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.place.datasource.AccountPlaceLocalDataSource
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
internal class AccountPlaceLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountPlaceLocalDataSource {
    override fun page(
        accountId: Uuid,
        query: String,
        sort: ListSortLocalEntity,
    ): PagingSource<Int, PlaceLocalEntity> =
        database.accountPlaceDao().page(
            accountId = accountId,
            query = query,
            sort = sort.queryValue,
        )

    override fun get(
        accountId: Uuid,
        placeIdSet: Set<Uuid>,
    ): Flow<List<PlaceLocalEntity>> =
        database.accountPlaceDao().get(
            accountId = accountId,
            placeIdSet = placeIdSet,
        )

    override fun get(
        accountId: Uuid,
        south: Double,
        north: Double,
        west: Double,
        east: Double,
        sort: ListSortLocalEntity,
    ): Flow<List<PlaceLocalEntity>> =
        database.accountPlaceDao().get(
            accountId = accountId,
            south = south,
            north = north,
            west = west,
            east = east,
            sort = sort.queryValue,
        )

    override fun find(
        accountId: Uuid,
        placeId: Uuid,
    ): Flow<PlaceLocalEntity?> =
        database.accountPlaceDao().find(
            accountId = accountId,
            placeId = placeId,
        )
}
