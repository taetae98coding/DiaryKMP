package io.github.taetae98coding.diary.core.database.impl.transaction

import androidx.room3.withWriteTransaction
import io.github.taetae98coding.diary.core.database.api.memoweb.entity.MemoWebLocalEntity
import io.github.taetae98coding.diary.core.database.api.memoweb.transaction.AccountMemoWebTransaction
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.entity.AccountMemoWebLocalEntity
import org.koin.core.annotation.Factory
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Factory
internal class AccountMemoWebTransactionImpl(
    private val database: DiaryDatabase,
) : AccountMemoWebTransaction {
    override suspend fun upsert(
        accountId: Uuid,
        memoId: Uuid,
        webId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ) {
        database.withWriteTransaction {
            if (isDeleted) {
                deleteWeb(accountId = accountId, memoId = memoId, webId = webId, updatedAt = updatedAt)
            } else {
                restoreWeb(accountId = accountId, memoId = memoId, webId = webId, updatedAt = updatedAt)
            }
        }
    }

    private suspend fun restoreWeb(
        accountId: Uuid,
        memoId: Uuid,
        webId: Uuid,
        updatedAt: Instant,
    ) {
        val restoredCount =
            database.memoWebDao().updateDeleted(
                memoId = memoId,
                webId = webId,
                isDeleted = false,
                updatedAt = updatedAt,
            )

        if (restoredCount == 0) {
            database.memoWebDao().upsert(
                MemoWebLocalEntity(
                    memoId = memoId,
                    webId = webId,
                    isDeleted = false,
                    updatedAt = updatedAt,
                    createdAt = updatedAt,
                ),
            )
        }

        database.accountMemoWebDao().upsert(
            AccountMemoWebLocalEntity(
                accountId = accountId,
                memoId = memoId,
                webId = webId,
                isDirty = true,
            ),
        )
    }

    private suspend fun deleteWeb(
        accountId: Uuid,
        memoId: Uuid,
        webId: Uuid,
        updatedAt: Instant,
    ) {
        val updatedCount =
            database.memoWebDao().updateDeleted(
                memoId = memoId,
                webId = webId,
                isDeleted = true,
                updatedAt = updatedAt,
            )

        if (updatedCount > 0) {
            database.accountMemoWebDao().markPending(accountId = accountId, memoId = memoId, webId = webId)
        }
    }
}
