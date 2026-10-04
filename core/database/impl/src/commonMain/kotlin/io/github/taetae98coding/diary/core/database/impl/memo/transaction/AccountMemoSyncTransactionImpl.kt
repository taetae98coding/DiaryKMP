package io.github.taetae98coding.diary.core.database.impl.memo.transaction

import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.transaction.AccountMemoSyncTransaction
import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.memo.entity.AccountMemoLocalEntity
import io.github.taetae98coding.diary.core.database.impl.sync.transaction.clearPendingEach
import io.github.taetae98coding.diary.core.database.impl.sync.transaction.upsertPulled
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountMemoSyncTransactionImpl(
    private val database: DiaryDatabase,
) : AccountMemoSyncTransaction {
    override suspend fun clearPending(
        accountId: Uuid,
        memoList: List<MemoLocalEntity>,
    ) {
        database.clearPendingEach(memoList) { memo ->
            database.accountMemoSyncDao().clearPending(
                accountId = accountId,
                memoId = memo.id,
                updatedAt = memo.updatedAt,
            )
        }
    }

    override suspend fun upsert(
        accountId: Uuid,
        memoList: List<MemoLocalEntity>,
        cursor: Long,
    ) {
        database.upsertPulled(
            accountId = accountId,
            kind = SyncKindLocalEntity.MEMO,
            cursor = cursor,
            pulledList = memoList,
            keyOf = { memo -> memo.id },
            updatedAtOf = { memo -> memo.updatedAt },
            readLocalUpdatedAtMap = { pulledList -> database.memoDao().findUpdatedAt(pulledList.map { memo -> memo.id }) },
            upsert = { upsertList -> database.memoDao().upsert(upsertList) },
            insertIgnoreAccount = { pulledList ->
                database.accountMemoSyncDao().insertIgnore(
                    pulledList.map { memo ->
                        AccountMemoLocalEntity(
                            accountId = accountId,
                            memoId = memo.id,
                            isDirty = false,
                        )
                    },
                )
            },
        )
    }
}
