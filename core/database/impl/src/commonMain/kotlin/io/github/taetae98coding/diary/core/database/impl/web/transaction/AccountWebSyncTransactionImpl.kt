package io.github.taetae98coding.diary.core.database.impl.web.transaction

import androidx.room3.withWriteTransaction
import io.github.taetae98coding.diary.core.database.api.sync.SyncKind
import io.github.taetae98coding.diary.core.database.api.web.entity.WebLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.transaction.AccountWebSyncTransaction
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.sync.entity.SyncCursorLocalEntity
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
        database.withWriteTransaction {
            webList.forEach { web ->
                database.accountWebSyncDao().clearPending(
                    accountId = accountId,
                    webId = web.id,
                    updatedAt = web.updatedAt,
                )
            }
        }
    }

    override suspend fun save(
        accountId: Uuid,
        webList: List<WebLocalEntity>,
        cursor: Long,
    ) {
        database.withWriteTransaction {
            val localUpdatedAtMap = database.webDao().findUpdatedAt(webList.map { web -> web.id })
            database.webDao().upsert(
                webList.filter { web ->
                    val localUpdatedAt = localUpdatedAtMap[web.id]
                    localUpdatedAt == null || web.updatedAt >= localUpdatedAt
                },
            )
            database.accountWebSyncDao().insertIgnore(
                webList.map { web ->
                    AccountWebLocalEntity(
                        accountId = accountId,
                        webId = web.id,
                        isDirty = false,
                    )
                },
            )
            database.syncCursorDao().upsert(
                SyncCursorLocalEntity(
                    accountId = accountId,
                    kind = SyncCursorLocalEntity.column(kind = SyncKind.WEB),
                    usn = cursor,
                ),
            )
        }
    }
}
