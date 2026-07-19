package io.github.taetae98coding.diary.core.database.impl.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.datasource.AccountWebLocalDataSource
import io.github.taetae98coding.diary.core.database.api.web.entity.WebLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountWebLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountWebLocalDataSource {
    override fun page(
        accountId: Uuid,
        sort: ListSortLocalEntity,
    ): PagingSource<Int, WebLocalEntity> =
        database.accountWebDao().page(
            accountId = accountId,
            sort = sort.queryValue,
        )

    override fun get(
        accountId: Uuid,
        webIdSet: Set<Uuid>,
    ): Flow<List<WebLocalEntity>> =
        database.accountWebDao().get(
            accountId = accountId,
            webIdSet = webIdSet,
        )

    override fun find(
        accountId: Uuid,
        webId: Uuid,
    ): Flow<WebLocalEntity?> =
        database.accountWebDao().find(
            accountId = accountId,
            webId = webId,
        )
}
