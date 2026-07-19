package io.github.taetae98coding.diary.core.database.impl.transaction

import androidx.room3.withWriteTransaction
import io.github.taetae98coding.diary.core.database.api.placetag.entity.PlaceTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.placetag.transaction.AccountPlaceTagTransaction
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.entity.AccountPlaceTagLocalEntity
import org.koin.core.annotation.Factory
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Factory
internal class AccountPlaceTagTransactionImpl(
    private val database: DiaryDatabase,
) : AccountPlaceTagTransaction {
    override suspend fun upsert(
        accountId: Uuid,
        placeId: Uuid,
        tagId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ) {
        database.withWriteTransaction {
            if (isDeleted) {
                deleteLink(accountId = accountId, placeId = placeId, tagId = tagId, updatedAt = updatedAt)
            } else {
                restoreLink(accountId = accountId, placeId = placeId, tagId = tagId, updatedAt = updatedAt)
            }
        }
    }

    private suspend fun restoreLink(
        accountId: Uuid,
        placeId: Uuid,
        tagId: Uuid,
        updatedAt: Instant,
    ) {
        val restoredCount =
            database.placeTagDao().updateDeleted(
                placeId = placeId,
                tagId = tagId,
                isDeleted = false,
                updatedAt = updatedAt,
            )

        if (restoredCount == 0) {
            database.placeTagDao().upsert(
                PlaceTagLocalEntity(
                    placeId = placeId,
                    tagId = tagId,
                    isDeleted = false,
                    updatedAt = updatedAt,
                    createdAt = updatedAt,
                ),
            )
        }

        database.accountPlaceTagDao().upsert(
            AccountPlaceTagLocalEntity(
                accountId = accountId,
                placeId = placeId,
                tagId = tagId,
                isDirty = true,
            ),
        )
    }

    private suspend fun deleteLink(
        accountId: Uuid,
        placeId: Uuid,
        tagId: Uuid,
        updatedAt: Instant,
    ) {
        val updatedCount =
            database.placeTagDao().updateDeleted(
                placeId = placeId,
                tagId = tagId,
                isDeleted = true,
                updatedAt = updatedAt,
            )

        if (updatedCount > 0) {
            database.accountPlaceTagDao().markPending(accountId = accountId, placeId = placeId, tagId = tagId)
        }
    }
}
