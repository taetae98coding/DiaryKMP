package io.github.taetae98coding.diary.core.database.impl.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import io.github.taetae98coding.diary.core.database.api.search.datasource.SearchPlaceLocalDataSource
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class SearchPlaceLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : SearchPlaceLocalDataSource {
    override fun page(
        accountId: Uuid,
        query: String,
        sort: ListSortLocalEntity,
    ): PagingSource<Int, PlaceLocalEntity> =
        database.searchPlaceDao().page(
            accountId = accountId,
            query = query,
            sort = sort.queryValue,
        )
}
