package io.github.taetae98coding.diary.core.database.impl.contact.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.contact.datasource.AccountTagContactLocalDataSource
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactListItemLocalEntity
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagScopeLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountTagContactLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountTagContactLocalDataSource {
    override fun page(
        accountId: Uuid,
        tagId: Uuid,
        scope: TagScopeLocalEntity,
        sort: ListSortLocalEntity,
    ): PagingSource<Int, ContactListItemLocalEntity> =
        database.accountTagContactDao().page(
            accountId = accountId,
            tagId = tagId,
            scope = scope.queryValue,
            sort = sort.queryValue,
        )
}
