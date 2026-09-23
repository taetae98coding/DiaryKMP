package io.github.taetae98coding.diary.core.database.impl.taglink.transaction

import androidx.room3.withWriteTransaction
import io.github.taetae98coding.diary.core.database.api.taglink.entity.TagLinkLocalEntity
import io.github.taetae98coding.diary.core.database.api.taglink.transaction.AccountTagLinkTransaction
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.taglink.entity.AccountTagLinkLocalEntity
import org.koin.core.annotation.Factory
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Factory
internal class AccountTagLinkTransactionImpl(
    private val database: DiaryDatabase,
) : AccountTagLinkTransaction {
    override suspend fun upsert(
        accountId: Uuid,
        fromTagId: Uuid,
        toTagId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ) {
        database.withWriteTransaction {
            if (isDeleted) {
                deleteLink(accountId = accountId, fromTagId = fromTagId, toTagId = toTagId, updatedAt = updatedAt)
            } else {
                restoreLink(accountId = accountId, fromTagId = fromTagId, toTagId = toTagId, updatedAt = updatedAt)
            }
        }
    }

    private suspend fun restoreLink(
        accountId: Uuid,
        fromTagId: Uuid,
        toTagId: Uuid,
        updatedAt: Instant,
    ) {
        val restoredCount =
            database.tagLinkDao().updateDeleted(
                fromTagId = fromTagId,
                toTagId = toTagId,
                isDeleted = false,
                updatedAt = updatedAt,
            )

        if (restoredCount == 0) {
            database.tagLinkDao().upsert(
                TagLinkLocalEntity(
                    fromTagId = fromTagId,
                    toTagId = toTagId,
                    isDeleted = false,
                    updatedAt = updatedAt,
                    createdAt = updatedAt,
                ),
            )
        }

        database.accountTagLinkDao().upsert(
            AccountTagLinkLocalEntity(
                accountId = accountId,
                fromTagId = fromTagId,
                toTagId = toTagId,
                isDirty = true,
            ),
        )
    }

    private suspend fun deleteLink(
        accountId: Uuid,
        fromTagId: Uuid,
        toTagId: Uuid,
        updatedAt: Instant,
    ) {
        val updatedCount =
            database.tagLinkDao().updateDeleted(
                fromTagId = fromTagId,
                toTagId = toTagId,
                isDeleted = true,
                updatedAt = updatedAt,
            )

        if (updatedCount > 0) {
            database.accountTagLinkDao().markPending(accountId = accountId, fromTagId = fromTagId, toTagId = toTagId)
        }
    }
}
