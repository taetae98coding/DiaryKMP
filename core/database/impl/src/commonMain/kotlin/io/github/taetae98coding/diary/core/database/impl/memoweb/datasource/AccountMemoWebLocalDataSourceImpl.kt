package io.github.taetae98coding.diary.core.database.impl.memoweb.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.memoweb.datasource.AccountMemoWebLocalDataSource
import io.github.taetae98coding.diary.core.database.api.web.entity.WebLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountMemoWebLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountMemoWebLocalDataSource {
    override fun getWebList(
        accountId: Uuid,
        memoId: Uuid,
    ): Flow<List<WebLocalEntity>> =
        database.accountMemoWebDao().getWebList(
            accountId = accountId,
            memoId = memoId,
        )

    override fun pageSelectableWeb(
        accountId: Uuid,
        query: String,
    ): PagingSource<Int, WebLocalEntity> =
        database.accountMemoWebDao().pageSelectableWeb(
            accountId = accountId,
            query = query,
        )

    override suspend fun findWebIdList(
        accountId: Uuid,
        memoId: Uuid,
    ): List<Uuid> =
        database.accountMemoWebDao().findWebIdList(
            accountId = accountId,
            memoId = memoId,
        )
}
