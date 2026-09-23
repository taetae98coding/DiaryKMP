package io.github.taetae98coding.diary.core.database.impl.memocontact.transaction

import androidx.room3.withWriteTransaction
import io.github.taetae98coding.diary.core.database.api.memocontact.entity.MemoContactLocalEntity
import io.github.taetae98coding.diary.core.database.api.memocontact.transaction.AccountMemoContactSyncTransaction
import io.github.taetae98coding.diary.core.database.api.sync.SyncKind
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.memocontact.entity.AccountMemoContactLocalEntity
import io.github.taetae98coding.diary.core.database.impl.sync.entity.SyncCursorLocalEntity
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountMemoContactSyncTransactionImpl(
    private val database: DiaryDatabase,
) : AccountMemoContactSyncTransaction {
    override suspend fun clearPending(
        accountId: Uuid,
        memoContactList: List<MemoContactLocalEntity>,
    ) {
        database.withWriteTransaction {
            memoContactList.forEach { memoContact ->
                database.accountMemoContactSyncDao().clearPending(
                    accountId = accountId,
                    memoId = memoContact.memoId,
                    contactId = memoContact.contactId,
                    updatedAt = memoContact.updatedAt,
                )
            }
        }
    }

    override suspend fun save(
        accountId: Uuid,
        memoContactList: List<MemoContactLocalEntity>,
        cursor: Long,
    ) {
        database.withWriteTransaction {
            val localMemoContactMap =
                database
                    .memoContactDao()
                    .findByMemoIdList(memoContactList.map { memoContact -> memoContact.memoId }.distinct())
                    .associateBy { memoContact -> memoContact.memoId to memoContact.contactId }

            database.memoContactDao().upsert(
                memoContactList.filter { memoContact ->
                    val localUpdatedAt = localMemoContactMap[memoContact.memoId to memoContact.contactId]?.updatedAt
                    localUpdatedAt == null || memoContact.updatedAt >= localUpdatedAt
                },
            )
            database.accountMemoContactSyncDao().insertIgnore(
                memoContactList.map { memoContact ->
                    AccountMemoContactLocalEntity(
                        accountId = accountId,
                        memoId = memoContact.memoId,
                        contactId = memoContact.contactId,
                        isDirty = false,
                    )
                },
            )
            database.syncCursorDao().upsert(
                SyncCursorLocalEntity(
                    accountId = accountId,
                    kind = SyncCursorLocalEntity.column(kind = SyncKind.MEMO_CONTACT),
                    usn = cursor,
                ),
            )
        }
    }
}
