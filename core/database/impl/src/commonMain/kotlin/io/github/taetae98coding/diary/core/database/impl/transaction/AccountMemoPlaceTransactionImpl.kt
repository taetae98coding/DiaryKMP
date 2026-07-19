package io.github.taetae98coding.diary.core.database.impl.transaction

import androidx.room3.withWriteTransaction
import io.github.taetae98coding.diary.core.database.api.memoplace.entity.MemoPlaceLocalEntity
import io.github.taetae98coding.diary.core.database.api.memoplace.transaction.AccountMemoPlaceTransaction
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.entity.AccountMemoPlaceLocalEntity
import org.koin.core.annotation.Factory
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Factory
internal class AccountMemoPlaceTransactionImpl(
    private val database: DiaryDatabase,
) : AccountMemoPlaceTransaction {
    override suspend fun upsert(
        accountId: Uuid,
        memoId: Uuid,
        placeId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ) {
        database.withWriteTransaction {
            if (isDeleted) {
                deletePlace(accountId = accountId, memoId = memoId, placeId = placeId, updatedAt = updatedAt)
            } else {
                restorePlace(accountId = accountId, memoId = memoId, placeId = placeId, updatedAt = updatedAt)
            }
        }
    }

    private suspend fun restorePlace(
        accountId: Uuid,
        memoId: Uuid,
        placeId: Uuid,
        updatedAt: Instant,
    ) {
        val restoredCount =
            database.memoPlaceDao().updateDeleted(
                memoId = memoId,
                placeId = placeId,
                isDeleted = false,
                updatedAt = updatedAt,
            )

        if (restoredCount == 0) {
            database.memoPlaceDao().upsert(
                MemoPlaceLocalEntity(
                    memoId = memoId,
                    placeId = placeId,
                    isDeleted = false,
                    updatedAt = updatedAt,
                    createdAt = updatedAt,
                ),
            )
        }

        database.accountMemoPlaceDao().upsert(
            AccountMemoPlaceLocalEntity(
                accountId = accountId,
                memoId = memoId,
                placeId = placeId,
                isDirty = true,
            ),
        )
    }

    private suspend fun deletePlace(
        accountId: Uuid,
        memoId: Uuid,
        placeId: Uuid,
        updatedAt: Instant,
    ) {
        val updatedCount =
            database.memoPlaceDao().updateDeleted(
                memoId = memoId,
                placeId = placeId,
                isDeleted = true,
                updatedAt = updatedAt,
            )

        if (updatedCount > 0) {
            database.accountMemoPlaceDao().markPending(accountId = accountId, memoId = memoId, placeId = placeId)
        }
    }
}
