package io.github.taetae98coding.diary.core.database.impl.transaction

import androidx.room3.withWriteTransaction
import io.github.taetae98coding.diary.core.database.api.memotag.entity.MemoTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.memotag.transaction.AccountMemoTagSyncTransaction
import io.github.taetae98coding.diary.core.database.api.sync.SyncKind
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.entity.AccountMemoTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entity.SyncCursorLocalEntity
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountMemoTagSyncTransactionImpl(
    private val database: DiaryDatabase,
) : AccountMemoTagSyncTransaction {
    override suspend fun clearPending(
        accountId: Uuid,
        memoTagList: List<MemoTagLocalEntity>,
    ) {
        database.withWriteTransaction {
            memoTagList.forEach { memoTag ->
                database.accountMemoTagSyncDao().clearPending(
                    accountId = accountId,
                    memoId = memoTag.memoId,
                    tagId = memoTag.tagId,
                    updatedAt = memoTag.updatedAt,
                )
            }
        }
    }

    override suspend fun save(
        accountId: Uuid,
        memoTagList: List<MemoTagLocalEntity>,
        cursor: Long,
    ) {
        database.withWriteTransaction {
            val localMemoTagMap =
                database
                    .memoTagDao()
                    .findByMemoIdList(memoTagList.map { memoTag -> memoTag.memoId }.distinct())
                    .associateBy { memoTag -> memoTag.memoId to memoTag.tagId }

            database.memoTagDao().upsert(
                memoTagList.filter { memoTag ->
                    val localUpdatedAt = localMemoTagMap[memoTag.memoId to memoTag.tagId]?.updatedAt
                    localUpdatedAt == null || memoTag.updatedAt >= localUpdatedAt
                },
            )
            database.accountMemoTagSyncDao().insertIgnore(
                memoTagList.map { memoTag ->
                    AccountMemoTagLocalEntity(
                        accountId = accountId,
                        memoId = memoTag.memoId,
                        tagId = memoTag.tagId,
                        isDirty = false,
                    )
                },
            )
            database.syncCursorDao().upsert(
                SyncCursorLocalEntity(
                    accountId = accountId,
                    kind = SyncCursorLocalEntity.column(kind = SyncKind.MEMO_TAG),
                    usn = cursor,
                ),
            )
        }
    }
}
