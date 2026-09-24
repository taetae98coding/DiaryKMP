package io.github.taetae98coding.diary.core.database.impl.memo.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.datasource.AccountPlaceMemoLocalDataSource
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
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
}
