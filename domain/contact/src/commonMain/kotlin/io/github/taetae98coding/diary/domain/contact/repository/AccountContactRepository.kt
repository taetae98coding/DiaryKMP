package io.github.taetae98coding.diary.domain.contact.repository

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import io.github.taetae98coding.diary.core.model.list.ListSort
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant
import kotlin.uuid.Uuid

public interface AccountContactRepository {
    public fun page(
        account: Account,
        sort: ListSort,
    ): Flow<PagingData<Contact>>

    public fun find(
        account: Account,
        contactId: Uuid,
    ): Flow<Contact?>

    public suspend fun upsert(
        account: Account,
        contact: Contact,
    )

    public suspend fun updateDetail(
        account: Account,
        contactId: Uuid,
        detail: ContactDetail,
        updatedAt: Instant,
    ): Int

    public suspend fun updateDeleted(
        account: Account,
        contactId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int
}
