package io.github.taetae98coding.diary.core.database.impl.memo.transaction

import androidx.room3.withWriteTransaction
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.transaction.AccountMemoSyncTransaction
import io.github.taetae98coding.diary.core.database.api.sync.SyncKind
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.memo.entity.AccountMemoLocalEntity
import io.github.taetae98coding.diary.core.database.impl.sync.entity.SyncCursorLocalEntity
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
        database.withWriteTransaction {
            memoList.forEach { memo ->
                database.accountMemoSyncDao().clearPending(
                    accountId = accountId,
                    memoId = memo.id,
                    updatedAt = memo.updatedAt,
                )
            }
        }
    }

    override suspend fun save(
        accountId: Uuid,
        memoList: List<MemoLocalEntity>,
        cursor: Long,
    ) {
        database.withWriteTransaction {
            val localUpdatedAtMap = database.memoDao().findUpdatedAt(memoList.map { memo -> memo.id })
            database.memoDao().upsert(
                memoList.filter { memo ->
                    val localUpdatedAt = localUpdatedAtMap[memo.id]
                    localUpdatedAt == null || memo.updatedAt >= localUpdatedAt
                },
            )
            database.accountMemoSyncDao().insertIgnore(
                memoList.map { memo ->
                    AccountMemoLocalEntity(
                        accountId = accountId,
                        memoId = memo.id,
                        isDirty = false,
                    )
                },
            )
            database.syncCursorDao().upsert(
                SyncCursorLocalEntity(
                    accountId = accountId,
                    kind = SyncCursorLocalEntity.column(kind = SyncKind.MEMO),
                    usn = cursor,
                ),
            )
        }
    }
}
