package io.github.taetae98coding.diary.core.database.api.search.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayLocalEntity
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import kotlin.uuid.Uuid

public interface SearchDDayLocalDataSource {
    public fun page(
        accountId: Uuid,
        query: String,
        sort: ListSortLocalEntity,
    ): PagingSource<Int, DDayLocalEntity>
}
