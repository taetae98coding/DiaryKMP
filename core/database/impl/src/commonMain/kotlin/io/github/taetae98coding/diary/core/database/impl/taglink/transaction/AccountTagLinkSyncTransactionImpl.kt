package io.github.taetae98coding.diary.core.database.impl.taglink.transaction

import androidx.room3.withWriteTransaction
import io.github.taetae98coding.diary.core.database.api.sync.SyncKind
import io.github.taetae98coding.diary.core.database.api.taglink.entity.TagLinkLocalEntity
import io.github.taetae98coding.diary.core.database.api.taglink.transaction.AccountTagLinkSyncTransaction
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.sync.entity.SyncCursorLocalEntity
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
        database.withWriteTransaction {
            tagLinkList.forEach { tagLink ->
                database.accountTagLinkSyncDao().clearPending(
                    accountId = accountId,
                    fromTagId = tagLink.fromTagId,
                    toTagId = tagLink.toTagId,
                    updatedAt = tagLink.updatedAt,
                )
            }
        }
    }

    override suspend fun save(
        accountId: Uuid,
        tagLinkList: List<TagLinkLocalEntity>,
        cursor: Long,
    ) {
        database.withWriteTransaction {
            val localTagLinkMap =
                database
                    .tagLinkDao()
                    .findByFromTagIdList(tagLinkList.map { tagLink -> tagLink.fromTagId }.distinct())
                    .associateBy { tagLink -> tagLink.fromTagId to tagLink.toTagId }

            database.tagLinkDao().upsert(
                tagLinkList.filter { tagLink ->
                    val localUpdatedAt = localTagLinkMap[tagLink.fromTagId to tagLink.toTagId]?.updatedAt
                    localUpdatedAt == null || tagLink.updatedAt >= localUpdatedAt
                },
            )
            database.accountTagLinkSyncDao().insertIgnore(
                tagLinkList.map { tagLink ->
                    AccountTagLinkLocalEntity(
                        accountId = accountId,
                        fromTagId = tagLink.fromTagId,
                        toTagId = tagLink.toTagId,
                        isDirty = false,
                    )
                },
            )
            database.syncCursorDao().upsert(
                SyncCursorLocalEntity(
                    accountId = accountId,
                    kind = SyncCursorLocalEntity.column(kind = SyncKind.TAG_LINK),
                    usn = cursor,
                ),
            )
        }
    }
}
