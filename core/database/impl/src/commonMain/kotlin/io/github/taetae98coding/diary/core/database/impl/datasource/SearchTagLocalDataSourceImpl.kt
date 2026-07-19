package io.github.taetae98coding.diary.core.database.impl.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.search.datasource.SearchTagLocalDataSource
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class SearchTagLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : SearchTagLocalDataSource {
    override fun page(
        accountId: Uuid,
        query: String,
        sort: ListSortLocalEntity,
    ): PagingSource<Int, TagLocalEntity> =
        database.searchTagDao().page(
            accountId = accountId,
            query = query,
            sort = sort.queryValue,
        )
}
