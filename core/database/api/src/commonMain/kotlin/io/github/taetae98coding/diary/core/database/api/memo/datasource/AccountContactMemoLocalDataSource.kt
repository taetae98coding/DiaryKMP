package io.github.taetae98coding.diary.core.database.api.memo.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import kotlin.uuid.Uuid

public interface AccountContactMemoLocalDataSource {
    public fun page(
        accountId: Uuid,
        contactId: Uuid,
        sort: ListSortLocalEntity,
    ): PagingSource<Int, MemoLocalEntity>
}
