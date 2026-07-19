package io.github.taetae98coding.diary.core.database.api.memoweb.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.web.entity.WebLocalEntity
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

public interface AccountMemoWebLocalDataSource {
    public fun getWebList(
        accountId: Uuid,
        memoId: Uuid,
    ): Flow<List<WebLocalEntity>>

    public fun pageSelectableWeb(
        accountId: Uuid,
        query: String,
    ): PagingSource<Int, WebLocalEntity>

    public suspend fun findWebIdList(
        accountId: Uuid,
        memoId: Uuid,
    ): List<Uuid>
}
