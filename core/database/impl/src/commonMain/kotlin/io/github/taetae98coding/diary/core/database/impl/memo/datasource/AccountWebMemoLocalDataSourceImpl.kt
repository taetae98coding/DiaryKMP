package io.github.taetae98coding.diary.core.database.impl.memo.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.datasource.AccountWebMemoLocalDataSource
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountWebMemoLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountWebMemoLocalDataSource {
    override fun page(
        accountId: Uuid,
        webId: Uuid,
        sort: ListSortLocalEntity,
    ): PagingSource<Int, MemoLocalEntity> =
        database.accountWebMemoDao().page(
            accountId = accountId,
            webId = webId,
            sort = sort.queryValue,
        )
}
