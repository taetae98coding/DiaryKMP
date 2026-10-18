package io.github.taetae98coding.diary.core.database.impl.memoweb.transaction

import io.github.taetae98coding.diary.core.database.api.memoweb.entity.MemoWebLocalEntity
import io.github.taetae98coding.diary.core.database.api.memoweb.transaction.AccountMemoWebSyncTransaction
import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.memoweb.entity.AccountMemoWebLocalEntity
import io.github.taetae98coding.diary.core.database.impl.sync.transaction.clearPendingEach
import io.github.taetae98coding.diary.core.database.impl.sync.transaction.upsertPulled
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountMemoWebSyncTransactionImpl(
    private val database: DiaryDatabase,
) : AccountMemoWebSyncTransaction {
    override suspend fun clearPending(
        accountId: Uuid,
        memoWebList: List<MemoWebLocalEntity>,
    ) {
        database.clearPendingEach(memoWebList) { memoWeb ->
            database.accountMemoWebSyncDao().clearPending(
                accountId = accountId,
                memoId = memoWeb.memoId,
                webId = memoWeb.webId,
                updatedAt = memoWeb.updatedAt,
            )
        }
    }

    override suspend fun upsert(
        accountId: Uuid,
        memoWebList: List<MemoWebLocalEntity>,
        cursor: Long,
    ) {
        database.upsertPulled(
            accountId = accountId,
            kind = SyncKindLocalEntity.MEMO_WEB,
            cursor = cursor,
            pulledList = memoWebList,
            keyOf = { memoWeb -> memoWeb.memoId to memoWeb.webId },
            updatedAtOf = { memoWeb -> memoWeb.updatedAt },
            readLocalUpdatedAtMap = { pulledList ->
                database
                    .memoWebDao()
                    .findByMemoIdList(pulledList.map { memoWeb -> memoWeb.memoId }.distinct())
                    .associate { memoWeb -> (memoWeb.memoId to memoWeb.webId) to memoWeb.updatedAt }
            },
            upsert = { upsertList -> database.memoWebDao().upsert(upsertList) },
            insertIgnoreAccount = { pulledList ->
                database.accountMemoWebSyncDao().insertIgnore(
                    pulledList.map { memoWeb ->
                        AccountMemoWebLocalEntity(
                            accountId = accountId,
                            memoId = memoWeb.memoId,
                            webId = memoWeb.webId,
                            isDirty = false,
                        )
                    },
                )
            },
        )
    }
}
