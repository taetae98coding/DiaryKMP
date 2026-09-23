package io.github.taetae98coding.diary.core.database.impl.memoweb.transaction

import androidx.room3.withWriteTransaction
import io.github.taetae98coding.diary.core.database.api.memoweb.entity.MemoWebLocalEntity
import io.github.taetae98coding.diary.core.database.api.memoweb.transaction.AccountMemoWebSyncTransaction
import io.github.taetae98coding.diary.core.database.api.sync.SyncKind
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.memoweb.entity.AccountMemoWebLocalEntity
import io.github.taetae98coding.diary.core.database.impl.sync.entity.SyncCursorLocalEntity
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
        database.withWriteTransaction {
            memoWebList.forEach { memoWeb ->
                database.accountMemoWebSyncDao().clearPending(
                    accountId = accountId,
                    memoId = memoWeb.memoId,
                    webId = memoWeb.webId,
                    updatedAt = memoWeb.updatedAt,
                )
            }
        }
    }

    override suspend fun save(
        accountId: Uuid,
        memoWebList: List<MemoWebLocalEntity>,
        cursor: Long,
    ) {
        database.withWriteTransaction {
            val localMemoWebMap =
                database
                    .memoWebDao()
                    .findByMemoIdList(memoWebList.map { memoWeb -> memoWeb.memoId }.distinct())
                    .associateBy { memoWeb -> memoWeb.memoId to memoWeb.webId }

            database.memoWebDao().upsert(
                memoWebList.filter { memoWeb ->
                    val localUpdatedAt = localMemoWebMap[memoWeb.memoId to memoWeb.webId]?.updatedAt
                    localUpdatedAt == null || memoWeb.updatedAt >= localUpdatedAt
                },
            )
            database.accountMemoWebSyncDao().insertIgnore(
                memoWebList.map { memoWeb ->
                    AccountMemoWebLocalEntity(
                        accountId = accountId,
                        memoId = memoWeb.memoId,
                        webId = memoWeb.webId,
                        isDirty = false,
                    )
                },
            )
            database.syncCursorDao().upsert(
                SyncCursorLocalEntity(
                    accountId = accountId,
                    kind = SyncCursorLocalEntity.column(kind = SyncKind.MEMO_WEB),
                    usn = cursor,
                ),
            )
        }
    }
}
