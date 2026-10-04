package io.github.taetae98coding.diary.core.database.impl.contact.transaction

import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.transaction.AccountContactSyncTransaction
import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.contact.entity.AccountContactLocalEntity
import io.github.taetae98coding.diary.core.database.impl.sync.transaction.clearPendingEach
import io.github.taetae98coding.diary.core.database.impl.sync.transaction.upsertPulled
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountContactSyncTransactionImpl(
    private val database: DiaryDatabase,
) : AccountContactSyncTransaction {
    override suspend fun clearPending(
        accountId: Uuid,
        contactList: List<ContactLocalEntity>,
    ) {
        database.clearPendingEach(contactList) { contact ->
            database.accountContactSyncDao().clearPending(
                accountId = accountId,
                contactId = contact.id,
                updatedAt = contact.updatedAt,
            )
        }
    }

    override suspend fun upsert(
        accountId: Uuid,
        contactList: List<ContactLocalEntity>,
        cursor: Long,
    ) {
        database.upsertPulled(
            accountId = accountId,
            kind = SyncKindLocalEntity.CONTACT,
            cursor = cursor,
            pulledList = contactList,
            keyOf = { contact -> contact.id },
            updatedAtOf = { contact -> contact.updatedAt },
            readLocalUpdatedAtMap = { pulledList -> database.contactDao().findUpdatedAt(pulledList.map { contact -> contact.id }) },
            upsert = { upsertList -> database.contactDao().upsert(upsertList) },
            insertIgnoreAccount = { pulledList ->
                database.accountContactSyncDao().insertIgnore(
                    pulledList.map { contact ->
                        AccountContactLocalEntity(
                            accountId = accountId,
                            contactId = contact.id,
                            isDirty = false,
                        )
                    },
                )
            },
        )
    }
}
