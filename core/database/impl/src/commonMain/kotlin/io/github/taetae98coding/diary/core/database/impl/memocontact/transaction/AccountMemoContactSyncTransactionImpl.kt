package io.github.taetae98coding.diary.core.database.impl.memocontact.transaction

import io.github.taetae98coding.diary.core.database.api.memocontact.entity.MemoContactLocalEntity
import io.github.taetae98coding.diary.core.database.api.memocontact.transaction.AccountMemoContactSyncTransaction
import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.memocontact.entity.AccountMemoContactLocalEntity
import io.github.taetae98coding.diary.core.database.impl.sync.transaction.clearPendingEach
import io.github.taetae98coding.diary.core.database.impl.sync.transaction.upsertPulled
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
        database.clearPendingEach(memoContactList) { memoContact ->
            database.accountMemoContactSyncDao().clearPending(
                accountId = accountId,
                memoId = memoContact.memoId,
                contactId = memoContact.contactId,
                updatedAt = memoContact.updatedAt,
            )
        }
    }

    override suspend fun upsert(
        accountId: Uuid,
        memoContactList: List<MemoContactLocalEntity>,
        cursor: Long,
    ) {
        database.upsertPulled(
            accountId = accountId,
            kind = SyncKindLocalEntity.MEMO_CONTACT,
            cursor = cursor,
            pulledList = memoContactList,
            keyOf = { memoContact -> memoContact.memoId to memoContact.contactId },
            updatedAtOf = { memoContact -> memoContact.updatedAt },
            readLocalUpdatedAtMap = { pulledList ->
                database
                    .memoContactDao()
                    .findByMemoIdList(pulledList.map { memoContact -> memoContact.memoId }.distinct())
                    .associate { memoContact -> (memoContact.memoId to memoContact.contactId) to memoContact.updatedAt }
            },
            upsert = { upsertList -> database.memoContactDao().upsert(upsertList) },
            insertIgnoreAccount = { pulledList ->
                database.accountMemoContactSyncDao().insertIgnore(
                    pulledList.map { memoContact ->
                        AccountMemoContactLocalEntity(
                            accountId = accountId,
                            memoId = memoContact.memoId,
                            contactId = memoContact.contactId,
                            isDirty = false,
                        )
                    },
                )
            },
        )
    }
}
