package io.github.taetae98coding.diary.core.database.impl.search.datasource

import androidx.paging.PagingSource
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.search.datasource.SearchWebLocalDataSource
import io.github.taetae98coding.diary.core.database.api.web.entity.WebLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
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
