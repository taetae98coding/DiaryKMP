package io.github.taetae98coding.diary.core.database.impl.search.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayLocalEntity
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.search.datasource.SearchDDayLocalDataSource
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class SearchDDayLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : SearchDDayLocalDataSource {
    override fun page(
        accountId: Uuid,
        query: String,
        sort: ListSortLocalEntity,
    ): PagingSource<Int, DDayLocalEntity> =
        database.searchDDayDao().page(
            accountId = accountId,
            query = query,
            sort = sort.queryValue,
        )
}
