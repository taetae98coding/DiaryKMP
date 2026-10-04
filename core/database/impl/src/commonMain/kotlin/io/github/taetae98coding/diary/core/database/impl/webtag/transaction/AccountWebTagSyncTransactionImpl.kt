package io.github.taetae98coding.diary.core.database.impl.webtag.transaction

import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.webtag.entity.WebTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.webtag.transaction.AccountWebTagSyncTransaction
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.sync.transaction.clearPendingEach
import io.github.taetae98coding.diary.core.database.impl.sync.transaction.upsertPulled
import io.github.taetae98coding.diary.core.database.impl.webtag.entity.AccountWebTagLocalEntity
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
        database.clearPendingEach(webTagList) { webTag ->
            database.accountWebTagSyncDao().clearPending(
                accountId = accountId,
                webId = webTag.webId,
                tagId = webTag.tagId,
                updatedAt = webTag.updatedAt,
            )
        }
    }

    override suspend fun upsert(
        accountId: Uuid,
        webTagList: List<WebTagLocalEntity>,
        cursor: Long,
    ) {
        database.upsertPulled(
            accountId = accountId,
            kind = SyncKindLocalEntity.WEB_TAG,
            cursor = cursor,
            pulledList = webTagList,
            keyOf = { webTag -> webTag.webId to webTag.tagId },
            updatedAtOf = { webTag -> webTag.updatedAt },
            readLocalUpdatedAtMap = { pulledList ->
                database
                    .webTagDao()
                    .findByWebIdList(pulledList.map { webTag -> webTag.webId }.distinct())
                    .associate { webTag -> (webTag.webId to webTag.tagId) to webTag.updatedAt }
            },
            upsert = { upsertList -> database.webTagDao().upsert(upsertList) },
            insertIgnoreAccount = { pulledList ->
                database.accountWebTagSyncDao().insertIgnore(
                    pulledList.map { webTag ->
                        AccountWebTagLocalEntity(
                            accountId = accountId,
                            webId = webTag.webId,
                            tagId = webTag.tagId,
                            isDirty = false,
                        )
                    },
                )
            },
        )
    }
}
