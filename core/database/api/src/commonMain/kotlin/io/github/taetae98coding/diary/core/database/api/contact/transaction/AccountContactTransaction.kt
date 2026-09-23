package io.github.taetae98coding.diary.core.database.api.contact.transaction

import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactLocalEntity
import kotlin.time.Instant
import kotlin.uuid.Uuid

public interface AccountContactTransaction {
    public suspend fun upsert(
        accountId: Uuid,
        contactList: List<ContactLocalEntity>,
    )

    public suspend fun updateDetail(
        accountId: Uuid,
        contactId: Uuid,
        detail: ContactDetailLocalEntity,
        updatedAt: Instant,
    ): Int

    public suspend fun updateFavorite(
        accountId: Uuid,
        contactId: Uuid,
        isFavorite: Boolean,
        updatedAt: Instant,
    ): Int

    public suspend fun updateDeleted(
        accountId: Uuid,
        contactId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int
}
