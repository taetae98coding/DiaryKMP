package io.github.taetae98coding.diary.core.database.impl.contact.transaction

import androidx.room3.withWriteTransaction
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.transaction.AccountContactSyncTransaction
import io.github.taetae98coding.diary.core.database.api.sync.SyncKind
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.contact.entity.AccountContactLocalEntity
import io.github.taetae98coding.diary.core.database.impl.sync.entity.SyncCursorLocalEntity
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
        database.withWriteTransaction {
            contactList.forEach { contact ->
                database.accountContactSyncDao().clearPending(
                    accountId = accountId,
                    contactId = contact.id,
                    updatedAt = contact.updatedAt,
                )
            }
        }
    }

    override suspend fun save(
        accountId: Uuid,
        contactList: List<ContactLocalEntity>,
        cursor: Long,
    ) {
        database.withWriteTransaction {
            val localUpdatedAtMap = database.contactDao().findUpdatedAt(contactList.map { contact -> contact.id })
            database.contactDao().upsert(
                contactList.filter { contact ->
                    val localUpdatedAt = localUpdatedAtMap[contact.id]
                    localUpdatedAt == null || contact.updatedAt >= localUpdatedAt
                },
            )
            database.accountContactSyncDao().insertIgnore(
                contactList.map { contact ->
                    AccountContactLocalEntity(
                        accountId = accountId,
                        contactId = contact.id,
                        isDirty = false,
                    )
                },
            )
            database.syncCursorDao().upsert(
                SyncCursorLocalEntity(
                    accountId = accountId,
                    kind = SyncCursorLocalEntity.column(kind = SyncKind.CONTACT),
                    usn = cursor,
                ),
            )
        }
    }
}
