package io.github.taetae98coding.diary.core.database.impl.web.transaction

import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.entity.WebLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.transaction.AccountWebSyncTransaction
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.sync.transaction.clearPendingEach
import io.github.taetae98coding.diary.core.database.impl.sync.transaction.upsertPulled
import io.github.taetae98coding.diary.core.database.impl.web.entity.AccountWebLocalEntity
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountWebSyncTransactionImpl(
    private val database: DiaryDatabase,
) : AccountWebSyncTransaction {
    override suspend fun clearPending(
        accountId: Uuid,
        webList: List<WebLocalEntity>,
    ) {
        database.clearPendingEach(webList) { web ->
            database.accountWebSyncDao().clearPending(
                accountId = accountId,
                webId = web.id,
                updatedAt = web.updatedAt,
            )
        }
    }

    override suspend fun upsert(
        accountId: Uuid,
        webList: List<WebLocalEntity>,
        cursor: Long,
    ) {
        database.upsertPulled(
            accountId = accountId,
            kind = SyncKindLocalEntity.WEB,
            cursor = cursor,
            pulledList = webList,
            keyOf = { web -> web.id },
            updatedAtOf = { web -> web.updatedAt },
            readLocalUpdatedAtMap = { pulledList -> database.webDao().findUpdatedAt(pulledList.map { web -> web.id }) },
            upsert = { upsertList -> database.webDao().upsert(upsertList) },
            insertIgnoreAccount = { pulledList ->
                database.accountWebSyncDao().insertIgnore(
                    pulledList.map { web ->
                        AccountWebLocalEntity(
                            accountId = accountId,
                            webId = web.id,
                            isDirty = false,
                        )
                    },
                )
            },
        )
    }
}
