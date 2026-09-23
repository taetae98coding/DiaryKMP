package io.github.taetae98coding.diary.core.database.impl.search.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.api.search.datasource.SearchMemoLocalDataSource
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class SearchMemoLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : SearchMemoLocalDataSource {
    override fun page(
        accountId: Uuid,
        query: String,
        sort: ListSortLocalEntity,
    ): PagingSource<Int, MemoLocalEntity> =
        database.searchMemoDao().page(
            accountId = accountId,
            query = query,
            sort = sort.queryValue,
        )
}
