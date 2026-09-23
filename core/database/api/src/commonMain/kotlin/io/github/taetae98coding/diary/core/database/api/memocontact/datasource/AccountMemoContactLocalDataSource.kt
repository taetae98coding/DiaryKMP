package io.github.taetae98coding.diary.core.database.api.memocontact.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactLocalEntity
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

public interface AccountMemoContactLocalDataSource {
    public fun getContactList(
        accountId: Uuid,
        memoId: Uuid,
    ): Flow<List<ContactLocalEntity>>

    public fun pageSelectableContact(
        accountId: Uuid,
        query: String,
    ): PagingSource<Int, ContactLocalEntity>

    public suspend fun findContactIdList(
        accountId: Uuid,
        memoId: Uuid,
    ): List<Uuid>
}
