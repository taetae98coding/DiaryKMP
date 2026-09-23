package io.github.taetae98coding.diary.core.database.impl.memotag.transaction

import androidx.room3.withWriteTransaction
import io.github.taetae98coding.diary.core.database.api.memotag.entity.MemoTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.memotag.transaction.AccountMemoTagTransaction
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.memotag.entity.AccountMemoTagLocalEntity
import org.koin.core.annotation.Factory
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Factory
internal class AccountMemoTagTransactionImpl(
    private val database: DiaryDatabase,
) : AccountMemoTagTransaction {
    override suspend fun upsert(
        accountId: Uuid,
        memoId: Uuid,
        tagId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ) {
        database.withWriteTransaction {
            if (isDeleted) {
                deleteTag(accountId = accountId, memoId = memoId, tagId = tagId, updatedAt = updatedAt)
            } else {
                restoreTag(accountId = accountId, memoId = memoId, tagId = tagId, updatedAt = updatedAt)
            }
        }
    }

    override suspend fun updatePrimaryTagId(
        accountId: Uuid,
        memoId: Uuid,
        primaryTagId: Uuid?,
        updatedAt: Instant,
    ) {
        database.withWriteTransaction {
            if (primaryTagId != null) {
                restoreTag(accountId = accountId, memoId = memoId, tagId = primaryTagId, updatedAt = updatedAt)
            }

            val updatedCount =
                database.accountMemoDao().updatePrimaryTagId(
                    accountId = accountId,
                    memoId = memoId,
                    primaryTagId = primaryTagId,
                    updatedAt = updatedAt,
                )
            if (updatedCount > 0) {
                database.accountMemoDao().markPending(accountId = accountId, memoId = memoId)
            }
        }
    }

    private suspend fun restoreTag(
        accountId: Uuid,
        memoId: Uuid,
        tagId: Uuid,
        updatedAt: Instant,
    ) {
        val restoredCount =
            database.memoTagDao().updateDeleted(
                memoId = memoId,
                tagId = tagId,
                isDeleted = false,
                updatedAt = updatedAt,
            )

        if (restoredCount == 0) {
            database.memoTagDao().upsert(
                MemoTagLocalEntity(
                    memoId = memoId,
                    tagId = tagId,
                    isDeleted = false,
                    updatedAt = updatedAt,
                    createdAt = updatedAt,
                ),
            )
        }

        database.accountMemoTagDao().upsert(
            AccountMemoTagLocalEntity(
                accountId = accountId,
                memoId = memoId,
                tagId = tagId,
                isDirty = true,
            ),
        )
    }

    private suspend fun deleteTag(
        accountId: Uuid,
        memoId: Uuid,
        tagId: Uuid,
        updatedAt: Instant,
    ) {
        val updatedCount =
            database.memoTagDao().updateDeleted(
                memoId = memoId,
                tagId = tagId,
                isDeleted = true,
                updatedAt = updatedAt,
            )
        if (updatedCount > 0) {
            database.accountMemoTagDao().markPending(accountId = accountId, memoId = memoId, tagId = tagId)
        }

        val clearedCount =
            database.accountMemoDao().clearPrimaryTagIdIfMatched(
                accountId = accountId,
                memoId = memoId,
                tagId = tagId,
                updatedAt = updatedAt,
            )
        if (clearedCount > 0) {
            database.accountMemoDao().markPending(accountId = accountId, memoId = memoId)
        }
    }
}
