package io.github.taetae98coding.diary.core.database.impl.transaction

import androidx.room3.withWriteTransaction
import io.github.taetae98coding.diary.core.database.api.sync.SyncKind
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.transaction.AccountTagSyncTransaction
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.entity.AccountTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entity.SyncCursorLocalEntity
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
        database.withWriteTransaction {
            tagList.forEach { tag ->
                database.accountTagSyncDao().clearPending(
                    accountId = accountId,
                    tagId = tag.id,
                    updatedAt = tag.updatedAt,
                )
            }
        }
    }

    override suspend fun save(
        accountId: Uuid,
        tagList: List<TagLocalEntity>,
        cursor: Long,
    ) {
        database.withWriteTransaction {
            val localUpdatedAtMap = database.tagDao().findUpdatedAt(tagList.map { tag -> tag.id })
            database.tagDao().upsert(
                tagList.filter { tag ->
                    val localUpdatedAt = localUpdatedAtMap[tag.id]
                    localUpdatedAt == null || tag.updatedAt >= localUpdatedAt
                },
            )
            database.accountTagSyncDao().insertIgnore(
                tagList.map { tag ->
                    AccountTagLocalEntity(
                        accountId = accountId,
                        tagId = tag.id,
                        isDirty = false,
                    )
                },
            )
            database.syncCursorDao().upsert(
                SyncCursorLocalEntity(
                    accountId = accountId,
                    kind = SyncCursorLocalEntity.column(kind = SyncKind.TAG),
                    usn = cursor,
                ),
            )
        }
    }
}
