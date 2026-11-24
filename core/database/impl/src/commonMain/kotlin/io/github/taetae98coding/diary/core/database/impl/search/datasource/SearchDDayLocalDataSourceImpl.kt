package io.github.taetae98coding.diary.core.database.impl.search.datasource

import androidx.paging.PagingSource
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayLocalEntity
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.search.datasource.SearchDDayLocalDataSource
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
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
