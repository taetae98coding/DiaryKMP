package io.github.taetae98coding.diary.core.database.impl.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.contact.datasource.AccountContactLocalDataSource
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactLocalEntity
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountContactLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountContactLocalDataSource {
    override fun page(
        accountId: Uuid,
        sort: ListSortLocalEntity,
    ): PagingSource<Int, ContactLocalEntity> =
        database.accountContactDao().page(
            accountId = accountId,
            sort = sort.queryValue,
        )

    override fun find(
        accountId: Uuid,
        contactId: Uuid,
    ): Flow<ContactLocalEntity?> =
        database.accountContactDao().find(
            accountId = accountId,
            contactId = contactId,
        )
}
