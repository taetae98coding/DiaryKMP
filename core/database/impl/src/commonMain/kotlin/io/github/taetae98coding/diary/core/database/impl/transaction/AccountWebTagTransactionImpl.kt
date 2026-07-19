package io.github.taetae98coding.diary.core.database.impl.transaction

import androidx.room3.withWriteTransaction
import io.github.taetae98coding.diary.core.database.api.webtag.entity.WebTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.webtag.transaction.AccountWebTagTransaction
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.entity.AccountWebTagLocalEntity
import org.koin.core.annotation.Factory
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Factory
internal class AccountWebTagTransactionImpl(
    private val database: DiaryDatabase,
) : AccountWebTagTransaction {
    override suspend fun upsert(
        accountId: Uuid,
        webId: Uuid,
        tagId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ) {
        database.withWriteTransaction {
            if (isDeleted) {
                deleteLink(accountId = accountId, webId = webId, tagId = tagId, updatedAt = updatedAt)
            } else {
                restoreLink(accountId = accountId, webId = webId, tagId = tagId, updatedAt = updatedAt)
            }
        }
    }

    private suspend fun restoreLink(
        accountId: Uuid,
        webId: Uuid,
        tagId: Uuid,
        updatedAt: Instant,
    ) {
        val restoredCount =
            database.webTagDao().updateDeleted(
                webId = webId,
                tagId = tagId,
                isDeleted = false,
                updatedAt = updatedAt,
            )

        if (restoredCount == 0) {
            database.webTagDao().upsert(
                WebTagLocalEntity(
                    webId = webId,
                    tagId = tagId,
                    isDeleted = false,
                    updatedAt = updatedAt,
                    createdAt = updatedAt,
                ),
            )
        }

        database.accountWebTagDao().upsert(
            AccountWebTagLocalEntity(
                accountId = accountId,
                webId = webId,
                tagId = tagId,
                isDirty = true,
            ),
        )
    }

    private suspend fun deleteLink(
        accountId: Uuid,
        webId: Uuid,
        tagId: Uuid,
        updatedAt: Instant,
    ) {
        val updatedCount =
            database.webTagDao().updateDeleted(
                webId = webId,
                tagId = tagId,
                isDeleted = true,
                updatedAt = updatedAt,
            )

        if (updatedCount > 0) {
            database.accountWebTagDao().markPending(accountId = accountId, webId = webId, tagId = tagId)
        }
    }
}
