package io.github.taetae98coding.diary.core.database.impl.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.search.datasource.SearchWebLocalDataSource
import io.github.taetae98coding.diary.core.database.api.web.entity.WebLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class SearchWebLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : SearchWebLocalDataSource {
    override fun page(
        accountId: Uuid,
        query: String,
        sort: ListSortLocalEntity,
    ): PagingSource<Int, WebLocalEntity> =
        database.searchWebDao().page(
            accountId = accountId,
            query = query,
            sort = sort.queryValue,
        )
}
