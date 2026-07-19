package io.github.taetae98coding.diary.core.database.impl.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.datasource.AccountTagMemoLocalDataSource
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagScopeLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountTagMemoLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountTagMemoLocalDataSource {
    override fun page(
        accountId: Uuid,
        tagId: Uuid,
        scope: TagScopeLocalEntity,
        sort: ListSortLocalEntity,
    ): PagingSource<Int, MemoLocalEntity> =
        database.accountTagMemoDao().page(
            accountId = accountId,
            tagId = tagId,
            scope = scope.queryValue,
            sort = sort.queryValue,
        )

    override fun pageFinished(
        accountId: Uuid,
        tagId: Uuid,
        sort: ListSortLocalEntity,
    ): PagingSource<Int, MemoLocalEntity> =
        database.accountTagMemoDao().pageFinished(
            accountId = accountId,
            tagId = tagId,
            sort = sort.queryValue,
        )
}
