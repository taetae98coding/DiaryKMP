package io.github.taetae98coding.diary.core.database.impl.web.datasource

import androidx.paging.PagingSource
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagScopeLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.datasource.AccountTagWebLocalDataSource
import io.github.taetae98coding.diary.core.database.api.web.entity.WebLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
internal class AccountTagWebLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountTagWebLocalDataSource {
    override fun page(
        accountId: Uuid,
        tagId: Uuid,
        scope: TagScopeLocalEntity,
        sort: ListSortLocalEntity,
    ): PagingSource<Int, WebLocalEntity> =
        database.accountTagWebDao().page(
            accountId = accountId,
            tagId = tagId,
            scope = scope.queryValue,
            sort = sort.queryValue,
        )
}
