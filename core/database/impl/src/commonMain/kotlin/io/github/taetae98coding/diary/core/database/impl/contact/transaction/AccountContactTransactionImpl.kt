package io.github.taetae98coding.diary.core.database.impl.contact.transaction

import androidx.room3.withWriteTransaction
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.transaction.AccountContactTransaction
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.contact.entity.AccountContactLocalEntity
import org.koin.core.annotation.Factory
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Factory
internal class AccountContactTransactionImpl(
    private val database: DiaryDatabase,
) : AccountContactTransaction {
    override suspend fun upsert(
        accountId: Uuid,
        contactList: List<ContactLocalEntity>,
    ) {
        database.withWriteTransaction {
            database.contactDao().upsert(contactList)
            database.accountContactDao().upsert(
                contactList.map { contact ->
                    AccountContactLocalEntity(
                        accountId = accountId,
                        contactId = contact.id,
                        isDirty = true,
                    )
                },
            )
        }
    }

    override suspend fun updateDetail(
        accountId: Uuid,
        contactId: Uuid,
        detail: ContactDetailLocalEntity,
        updatedAt: Instant,
    ): Int =
        updateAndMarkPending(
            accountId = accountId,
            contactId = contactId,
        ) {
            database.accountContactDao().updateDetail(
                accountId = accountId,
                contactId = contactId,
                name = detail.name,
                description = detail.description,
                heightCentimeter = detail.heightCentimeter,
                footSizeMillimeter = detail.footSizeMillimeter,
                birthday = detail.birthday,
                birthdayCalendar = detail.birthdayCalendar,
                phoneNumberList = detail.phoneNumberList,
                updatedAt = updatedAt,
            )
        }

    override suspend fun updateDeleted(
        accountId: Uuid,
        contactId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int =
        updateAndMarkPending(
            accountId = accountId,
            contactId = contactId,
        ) {
            database.accountContactDao().updateDeleted(
                accountId = accountId,
                contactId = contactId,
                isDeleted = isDeleted,
                updatedAt = updatedAt,
            )
        }

    private suspend fun updateAndMarkPending(
        accountId: Uuid,
        contactId: Uuid,
        update: suspend () -> Int,
    ): Int =
        database.withWriteTransaction {
            val updatedCount = update()

            if (updatedCount > 0) {
                database.accountContactDao().markPending(accountId = accountId, contactId = contactId)
            }

            updatedCount
        }
}
