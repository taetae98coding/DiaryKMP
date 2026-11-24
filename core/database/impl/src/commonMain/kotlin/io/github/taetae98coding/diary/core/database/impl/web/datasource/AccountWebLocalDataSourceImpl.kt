package io.github.taetae98coding.diary.core.database.impl.web.datasource

import androidx.paging.PagingSource
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.datasource.AccountWebLocalDataSource
import io.github.taetae98coding.diary.core.database.api.web.entity.WebLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
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
