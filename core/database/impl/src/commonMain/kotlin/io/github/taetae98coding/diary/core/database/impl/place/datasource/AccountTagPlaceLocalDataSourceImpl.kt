package io.github.taetae98coding.diary.core.database.impl.place.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.place.datasource.AccountTagPlaceLocalDataSource
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagScopeLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountTagPlaceLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountTagPlaceLocalDataSource {
    override fun page(
        accountId: Uuid,
        tagId: Uuid,
        scope: TagScopeLocalEntity,
        sort: ListSortLocalEntity,
    ): PagingSource<Int, PlaceLocalEntity> =
        database.accountTagPlaceDao().page(
            accountId = accountId,
            tagId = tagId,
            scope = scope.queryValue,
            sort = sort.queryValue,
        )

    override fun get(
        accountId: Uuid,
        tagId: Uuid,
        scope: TagScopeLocalEntity,
        south: Double,
        north: Double,
        west: Double,
        east: Double,
        sort: ListSortLocalEntity,
    ): Flow<List<PlaceLocalEntity>> =
        database.accountTagPlaceDao().get(
            accountId = accountId,
            tagId = tagId,
            scope = scope.queryValue,
            south = south,
            north = north,
            west = west,
            east = east,
            sort = sort.queryValue,
        )
}
