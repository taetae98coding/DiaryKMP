package io.github.taetae98coding.diary.core.database.impl.web.transaction

import androidx.room3.withWriteTransaction
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.entity.WebDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.entity.WebLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.transaction.AccountWebTransaction
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.web.entity.AccountWebLocalEntity
import io.github.taetae98coding.diary.core.database.impl.webtag.webTagTable
import org.koin.core.annotation.Factory
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Factory
internal class AccountWebTransactionImpl(
    private val database: DiaryDatabase,
) : AccountWebTransaction {
    override suspend fun upsert(
        accountId: Uuid,
        webList: List<WebLocalEntity>,
        webTagList: List<EntityTagLocalEntity>,
    ) {
        database.withWriteTransaction {
            database.webDao().upsert(webList)
            database.accountWebDao().upsert(
                webList.map { web ->
                    AccountWebLocalEntity(
                        accountId = accountId,
                        webId = web.id,
                        isDirty = true,
                    )
                },
            )
            database.webTagTable().upsertPending(accountId = accountId, entityTagList = webTagList)
        }
    }

    override suspend fun updateDetail(
        accountId: Uuid,
        webId: Uuid,
        detail: WebDetailLocalEntity,
        updatedAt: Instant,
    ): Int =
        updateAndMarkPending(
            accountId = accountId,
            webId = webId,
        ) {
            database.accountWebDao().updateDetail(
                accountId = accountId,
                webId = webId,
                title = detail.title,
                description = detail.description,
                url = detail.url,
                headerList = detail.headerList,
                updatedAt = updatedAt,
            )
        }

    override suspend fun updateDeleted(
        accountId: Uuid,
        webId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int =
        updateAndMarkPending(
            accountId = accountId,
            webId = webId,
        ) {
            database.accountWebDao().updateDeleted(
                accountId = accountId,
                webId = webId,
                isDeleted = isDeleted,
                updatedAt = updatedAt,
            )
        }

    private suspend fun updateAndMarkPending(
        accountId: Uuid,
        webId: Uuid,
        update: suspend () -> Int,
    ): Int =
        database.withWriteTransaction {
            val updatedCount = update()

            if (updatedCount > 0) {
                database.accountWebDao().markPending(accountId = accountId, webId = webId)
            }

            updatedCount
        }
}
