package io.github.taetae98coding.diary.core.database.api.memo.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import kotlin.uuid.Uuid

public interface AccountWebMemoLocalDataSource {
    public fun page(
        accountId: Uuid,
        webId: Uuid,
        sort: ListSortLocalEntity,
    ): PagingSource<Int, MemoLocalEntity>
}
