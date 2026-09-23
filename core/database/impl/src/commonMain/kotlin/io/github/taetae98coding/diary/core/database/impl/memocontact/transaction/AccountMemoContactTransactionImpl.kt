package io.github.taetae98coding.diary.core.database.impl.memocontact.transaction

import androidx.room3.withWriteTransaction
import io.github.taetae98coding.diary.core.database.api.memocontact.entity.MemoContactLocalEntity
import io.github.taetae98coding.diary.core.database.api.memocontact.transaction.AccountMemoContactTransaction
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.memocontact.entity.AccountMemoContactLocalEntity
import org.koin.core.annotation.Factory
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Factory
internal class AccountMemoContactTransactionImpl(
    private val database: DiaryDatabase,
) : AccountMemoContactTransaction {
    override suspend fun upsert(
        accountId: Uuid,
        memoId: Uuid,
        contactId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ) {
        database.withWriteTransaction {
            if (isDeleted) {
                deleteContact(accountId = accountId, memoId = memoId, contactId = contactId, updatedAt = updatedAt)
            } else {
                restoreContact(accountId = accountId, memoId = memoId, contactId = contactId, updatedAt = updatedAt)
            }
        }
    }

    private suspend fun restoreContact(
        accountId: Uuid,
        memoId: Uuid,
        contactId: Uuid,
        updatedAt: Instant,
    ) {
        val restoredCount =
            database.memoContactDao().updateDeleted(
                memoId = memoId,
                contactId = contactId,
                isDeleted = false,
                updatedAt = updatedAt,
            )

        if (restoredCount == 0) {
            database.memoContactDao().upsert(
                MemoContactLocalEntity(
                    memoId = memoId,
                    contactId = contactId,
                    isDeleted = false,
                    updatedAt = updatedAt,
                    createdAt = updatedAt,
                ),
            )
        }

        database.accountMemoContactDao().upsert(
            AccountMemoContactLocalEntity(
                accountId = accountId,
                memoId = memoId,
                contactId = contactId,
                isDirty = true,
            ),
        )
    }

    private suspend fun deleteContact(
        accountId: Uuid,
        memoId: Uuid,
        contactId: Uuid,
        updatedAt: Instant,
    ) {
        val updatedCount =
            database.memoContactDao().updateDeleted(
                memoId = memoId,
                contactId = contactId,
                isDeleted = true,
                updatedAt = updatedAt,
            )

        if (updatedCount > 0) {
            database.accountMemoContactDao().markPending(accountId = accountId, memoId = memoId, contactId = contactId)
        }
    }
}
