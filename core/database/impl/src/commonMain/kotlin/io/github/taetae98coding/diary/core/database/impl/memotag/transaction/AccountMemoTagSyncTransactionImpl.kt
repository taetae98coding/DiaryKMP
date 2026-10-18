package io.github.taetae98coding.diary.core.database.impl.memotag.transaction

import io.github.taetae98coding.diary.core.database.api.memotag.entity.MemoTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.memotag.transaction.AccountMemoTagSyncTransaction
import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.memotag.entity.AccountMemoTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.sync.transaction.clearPendingEach
import io.github.taetae98coding.diary.core.database.impl.sync.transaction.upsertPulled
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
        database.clearPendingEach(memoTagList) { memoTag ->
            database.accountMemoTagSyncDao().clearPending(
                accountId = accountId,
                memoId = memoTag.memoId,
                tagId = memoTag.tagId,
                updatedAt = memoTag.updatedAt,
            )
        }
    }

    override suspend fun upsert(
        accountId: Uuid,
        memoTagList: List<MemoTagLocalEntity>,
        cursor: Long,
    ) {
        database.upsertPulled(
            accountId = accountId,
            kind = SyncKindLocalEntity.MEMO_TAG,
            cursor = cursor,
            pulledList = memoTagList,
            keyOf = { memoTag -> memoTag.memoId to memoTag.tagId },
            updatedAtOf = { memoTag -> memoTag.updatedAt },
            readLocalUpdatedAtMap = { pulledList ->
                database
                    .memoTagDao()
                    .findByMemoIdList(pulledList.map { memoTag -> memoTag.memoId }.distinct())
                    .associate { memoTag -> (memoTag.memoId to memoTag.tagId) to memoTag.updatedAt }
            },
            upsert = { upsertList -> database.memoTagDao().upsert(upsertList) },
            insertIgnoreAccount = { pulledList ->
                database.accountMemoTagSyncDao().insertIgnore(
                    pulledList.map { memoTag ->
                        AccountMemoTagLocalEntity(
                            accountId = accountId,
                            memoId = memoTag.memoId,
                            tagId = memoTag.tagId,
                            isDirty = false,
                        )
                    },
                )
            },
        )
    }
}
