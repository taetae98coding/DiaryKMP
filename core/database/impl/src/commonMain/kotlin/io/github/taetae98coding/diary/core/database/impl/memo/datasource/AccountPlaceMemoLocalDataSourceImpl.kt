package io.github.taetae98coding.diary.core.database.impl.memo.datasource

import androidx.paging.PagingSource
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.datasource.AccountPlaceMemoLocalDataSource
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
internal class AccountPlaceMemoLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountPlaceMemoLocalDataSource {
    override fun page(
        accountId: Uuid,
        placeId: Uuid,
        sort: ListSortLocalEntity,
    ): PagingSource<Int, MemoLocalEntity> =
        database.accountPlaceMemoDao().page(
            accountId = accountId,
            placeId = placeId,
            sort = sort.queryValue,
        )

    override fun pageFinished(
        accountId: Uuid,
        placeId: Uuid,
        sort: ListSortLocalEntity,
    ): PagingSource<Int, MemoLocalEntity> =
        database.accountPlaceMemoDao().pageFinished(
            accountId = accountId,
            placeId = placeId,
            sort = sort.queryValue,
        )
}
