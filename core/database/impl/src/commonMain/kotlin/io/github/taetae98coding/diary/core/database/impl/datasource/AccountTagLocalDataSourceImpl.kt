package io.github.taetae98coding.diary.core.database.impl.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.datasource.AccountTagLocalDataSource
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountTagLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountTagLocalDataSource {
    override fun get(
        accountId: Uuid,
        tagIdSet: Set<Uuid>,
    ): Flow<List<TagLocalEntity>> =
        database.accountTagDao().get(
            accountId = accountId,
            tagIdSet = tagIdSet,
        )

    override fun page(
        accountId: Uuid,
        query: String,
        sort: ListSortLocalEntity,
    ): PagingSource<Int, TagLocalEntity> =
        database.accountTagDao().page(
            accountId = accountId,
            query = query,
            sort = sort.queryValue,
        )

    override fun pageTopLevel(
        accountId: Uuid,
        sort: ListSortLocalEntity,
    ): PagingSource<Int, TagLocalEntity> =
        database.accountTagDao().pageTopLevel(
            accountId = accountId,
            sort = sort.queryValue,
        )

    override fun pageFinished(
        accountId: Uuid,
        sort: ListSortLocalEntity,
    ): PagingSource<Int, TagLocalEntity> =
        database.accountTagDao().pageFinished(
            accountId = accountId,
            sort = sort.queryValue,
        )

    override fun find(
        accountId: Uuid,
        tagId: Uuid,
    ): Flow<TagLocalEntity?> =
        database.accountTagDao().find(
            accountId = accountId,
            tagId = tagId,
        )
}
