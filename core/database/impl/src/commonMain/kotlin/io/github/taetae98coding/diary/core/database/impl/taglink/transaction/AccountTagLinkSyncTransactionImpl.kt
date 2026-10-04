package io.github.taetae98coding.diary.core.database.impl.taglink.transaction

import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.taglink.entity.TagLinkLocalEntity
import io.github.taetae98coding.diary.core.database.api.taglink.transaction.AccountTagLinkSyncTransaction
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.sync.transaction.clearPendingEach
import io.github.taetae98coding.diary.core.database.impl.sync.transaction.upsertPulled
import io.github.taetae98coding.diary.core.database.impl.taglink.entity.AccountTagLinkLocalEntity
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountTagLinkSyncTransactionImpl(
    private val database: DiaryDatabase,
) : AccountTagLinkSyncTransaction {
    override suspend fun clearPending(
        accountId: Uuid,
        tagLinkList: List<TagLinkLocalEntity>,
    ) {
        database.clearPendingEach(tagLinkList) { tagLink ->
            database.accountTagLinkSyncDao().clearPending(
                accountId = accountId,
                fromTagId = tagLink.fromTagId,
                toTagId = tagLink.toTagId,
                updatedAt = tagLink.updatedAt,
            )
        }
    }

    override suspend fun upsert(
        accountId: Uuid,
        tagLinkList: List<TagLinkLocalEntity>,
        cursor: Long,
    ) {
        database.upsertPulled(
            accountId = accountId,
            kind = SyncKindLocalEntity.TAG_LINK,
            cursor = cursor,
            pulledList = tagLinkList,
            keyOf = { tagLink -> tagLink.fromTagId to tagLink.toTagId },
            updatedAtOf = { tagLink -> tagLink.updatedAt },
            readLocalUpdatedAtMap = { pulledList ->
                database
                    .tagLinkDao()
                    .findByFromTagIdList(pulledList.map { tagLink -> tagLink.fromTagId }.distinct())
                    .associate { tagLink -> (tagLink.fromTagId to tagLink.toTagId) to tagLink.updatedAt }
            },
            upsert = { upsertList -> database.tagLinkDao().upsert(upsertList) },
            insertIgnoreAccount = { pulledList ->
                database.accountTagLinkSyncDao().insertIgnore(
                    pulledList.map { tagLink ->
                        AccountTagLinkLocalEntity(
                            accountId = accountId,
                            fromTagId = tagLink.fromTagId,
                            toTagId = tagLink.toTagId,
                            isDirty = false,
                        )
                    },
                )
            },
        )
    }
}
