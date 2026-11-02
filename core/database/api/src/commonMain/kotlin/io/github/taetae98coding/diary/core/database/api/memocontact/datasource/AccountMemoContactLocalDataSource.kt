package io.github.taetae98coding.diary.core.database.api.memocontact.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactListItemLocalEntity
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

public interface AccountMemoContactLocalDataSource {
    public fun getContactList(
        accountId: Uuid,
        memoId: Uuid,
    ): Flow<List<ContactListItemLocalEntity>>

    public fun pageSelectableContact(
        accountId: Uuid,
        query: String,
    ): PagingSource<Int, ContactListItemLocalEntity>

    public suspend fun readContactIdList(
        accountId: Uuid,
        memoId: Uuid,
    ): List<Uuid>
}
