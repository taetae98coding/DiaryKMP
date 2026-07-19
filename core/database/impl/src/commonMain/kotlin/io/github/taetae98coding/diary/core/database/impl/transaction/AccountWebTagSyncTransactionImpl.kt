package io.github.taetae98coding.diary.core.database.impl.transaction

import androidx.room3.withWriteTransaction
import io.github.taetae98coding.diary.core.database.api.sync.SyncKind
import io.github.taetae98coding.diary.core.database.api.webtag.entity.WebTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.webtag.transaction.AccountWebTagSyncTransaction
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.entity.AccountWebTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entity.SyncCursorLocalEntity
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountWebTagSyncTransactionImpl(
    private val database: DiaryDatabase,
) : AccountWebTagSyncTransaction {
    override suspend fun clearPending(
        accountId: Uuid,
        webTagList: List<WebTagLocalEntity>,
    ) {
        database.withWriteTransaction {
            webTagList.forEach { webTag ->
                database.accountWebTagSyncDao().clearPending(
                    accountId = accountId,
                    webId = webTag.webId,
                    tagId = webTag.tagId,
                    updatedAt = webTag.updatedAt,
                )
            }
        }
    }

    override suspend fun save(
        accountId: Uuid,
        webTagList: List<WebTagLocalEntity>,
        cursor: Long,
    ) {
        database.withWriteTransaction {
            val localWebTagMap =
                database
                    .webTagDao()
                    .findByWebIdList(webTagList.map { webTag -> webTag.webId }.distinct())
                    .associateBy { webTag -> webTag.webId to webTag.tagId }

            database.webTagDao().upsert(
                webTagList.filter { webTag ->
                    val localUpdatedAt = localWebTagMap[webTag.webId to webTag.tagId]?.updatedAt
                    localUpdatedAt == null || webTag.updatedAt >= localUpdatedAt
                },
            )
            database.accountWebTagSyncDao().insertIgnore(
                webTagList.map { webTag ->
                    AccountWebTagLocalEntity(
                        accountId = accountId,
                        webId = webTag.webId,
                        tagId = webTag.tagId,
                        isDirty = false,
                    )
                },
            )
            database.syncCursorDao().upsert(
                SyncCursorLocalEntity(
                    accountId = accountId,
                    kind = SyncCursorLocalEntity.column(kind = SyncKind.WEB_TAG),
                    usn = cursor,
                ),
            )
        }
    }
}
