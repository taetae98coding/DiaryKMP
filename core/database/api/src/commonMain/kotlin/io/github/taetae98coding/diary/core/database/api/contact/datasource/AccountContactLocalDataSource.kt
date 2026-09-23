package io.github.taetae98coding.diary.core.database.api.contact.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactLocalEntity
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

public interface AccountContactLocalDataSource {
    public fun page(
        accountId: Uuid,
        sort: ListSortLocalEntity,
    ): PagingSource<Int, ContactLocalEntity>

    public fun get(
        accountId: Uuid,
        contactIdSet: Set<Uuid>,
    ): Flow<List<ContactLocalEntity>>

    public fun find(
        accountId: Uuid,
        contactId: Uuid,
    ): Flow<ContactLocalEntity?>
}
