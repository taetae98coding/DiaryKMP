package io.github.taetae98coding.diary.core.database.impl.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.place.datasource.AccountPlaceLocalDataSource
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
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
