package io.github.taetae98coding.diary.core.database.impl.memo.datasource

import androidx.paging.PagingSource
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.datasource.AccountWebMemoLocalDataSource
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
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

    override fun pageFinished(
        accountId: Uuid,
        webId: Uuid,
        sort: ListSortLocalEntity,
    ): PagingSource<Int, MemoLocalEntity> =
        database.accountWebMemoDao().pageFinished(
            accountId = accountId,
            webId = webId,
            sort = sort.queryValue,
        )
}
