package io.github.taetae98coding.diary.core.database.impl.memo.datasource

import androidx.paging.PagingSource
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.datasource.AccountContactMemoLocalDataSource
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
internal class AccountContactMemoLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountContactMemoLocalDataSource {
    override fun page(
        accountId: Uuid,
        contactId: Uuid,
        sort: ListSortLocalEntity,
    ): PagingSource<Int, MemoLocalEntity> =
        database.accountContactMemoDao().page(
            accountId = accountId,
            contactId = contactId,
            sort = sort.queryValue,
        )

    override fun pageFinished(
        accountId: Uuid,
        contactId: Uuid,
        sort: ListSortLocalEntity,
    ): PagingSource<Int, MemoLocalEntity> =
        database.accountContactMemoDao().pageFinished(
            accountId = accountId,
            contactId = contactId,
            sort = sort.queryValue,
        )
}
