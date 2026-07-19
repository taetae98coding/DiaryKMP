package io.github.taetae98coding.diary.core.database.api.contact.transaction

import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactLocalEntity
import kotlin.uuid.Uuid

public interface AccountContactSyncTransaction {
    public suspend fun clearPending(
        accountId: Uuid,
        contactList: List<ContactLocalEntity>,
    )

    public suspend fun save(
        accountId: Uuid,
        contactList: List<ContactLocalEntity>,
        cursor: Long,
    )
}
