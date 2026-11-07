package io.github.taetae98coding.diary.domain.memo.repository

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.contact.ContactListItem
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant
import kotlin.uuid.Uuid

public interface AccountMemoContactRepository {
    public fun getContactList(
        account: Account,
        memoId: Uuid,
    ): Flow<List<ContactListItem>>

    public fun pageSelectableContact(
        account: Account,
        query: String,
    ): Flow<PagingData<ContactListItem>>

    public suspend fun readContactIdSet(
        account: Account,
        memoId: Uuid,
    ): Set<Uuid>

    public suspend fun upsert(
        account: Account,
        memoId: Uuid,
        contactId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    )
}
