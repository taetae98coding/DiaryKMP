package io.github.taetae98coding.diary.core.database.impl.tag.transaction

import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.transaction.AccountTagSyncTransaction
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.sync.transaction.clearPendingEach
import io.github.taetae98coding.diary.core.database.impl.sync.transaction.upsertPulled
import io.github.taetae98coding.diary.core.database.impl.tag.entity.AccountTagLocalEntity
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountTagSyncTransactionImpl(
    private val database: DiaryDatabase,
) : AccountTagSyncTransaction {
    override suspend fun clearPending(
        accountId: Uuid,
        tagList: List<TagLocalEntity>,
    ) {
        database.clearPendingEach(tagList) { tag ->
            database.accountTagSyncDao().clearPending(
                accountId = accountId,
                tagId = tag.id,
                updatedAt = tag.updatedAt,
            )
        }
    }

    override suspend fun upsert(
        accountId: Uuid,
        tagList: List<TagLocalEntity>,
        cursor: Long,
    ) {
        database.upsertPulled(
            accountId = accountId,
            kind = SyncKindLocalEntity.TAG,
            cursor = cursor,
            pulledList = tagList,
            keyOf = { tag -> tag.id },
            updatedAtOf = { tag -> tag.updatedAt },
            readLocalUpdatedAtMap = { pulledList -> database.tagDao().findUpdatedAt(pulledList.map { tag -> tag.id }) },
            upsert = { upsertList -> database.tagDao().upsert(upsertList) },
            insertIgnoreAccount = { pulledList ->
                database.accountTagSyncDao().insertIgnore(
                    pulledList.map { tag ->
                        AccountTagLocalEntity(
                            accountId = accountId,
                            tagId = tag.id,
                            isDirty = false,
                        )
                    },
                )
            },
        )
    }
}
