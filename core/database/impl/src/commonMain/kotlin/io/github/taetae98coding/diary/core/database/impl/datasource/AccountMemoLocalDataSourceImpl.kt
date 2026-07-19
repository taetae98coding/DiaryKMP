package io.github.taetae98coding.diary.core.database.impl.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.datasource.AccountMemoLocalDataSource
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountMemoLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountMemoLocalDataSource {
    override fun page(
        accountId: Uuid,
        sort: ListSortLocalEntity,
    ): PagingSource<Int, MemoLocalEntity> =
        database.accountMemoDao().page(
            accountId = accountId,
            sort = sort.queryValue,
        )

    override fun pageFinished(
        accountId: Uuid,
        sort: ListSortLocalEntity,
    ): PagingSource<Int, MemoLocalEntity> =
        database.accountMemoDao().pageFinished(
            accountId = accountId,
            sort = sort.queryValue,
        )

    override fun find(
        accountId: Uuid,
        memoId: Uuid,
    ): Flow<MemoLocalEntity?> =
        database.accountMemoDao().find(
            accountId = accountId,
            memoId = memoId,
        )
}
