package io.github.taetae98coding.diary.core.database.impl.checklistitem.transaction

import io.github.taetae98coding.diary.core.database.api.checklistitem.entity.ChecklistItemLocalEntity
import io.github.taetae98coding.diary.core.database.api.checklistitem.transaction.AccountChecklistItemSyncTransaction
import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.checklistitem.entity.AccountChecklistItemLocalEntity
import io.github.taetae98coding.diary.core.database.impl.sync.transaction.clearPendingEach
import io.github.taetae98coding.diary.core.database.impl.sync.transaction.upsertPulled
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountChecklistItemSyncTransactionImpl(
    private val database: DiaryDatabase,
) : AccountChecklistItemSyncTransaction {
    override suspend fun clearPending(
        accountId: Uuid,
        itemList: List<ChecklistItemLocalEntity>,
    ) {
        database.clearPendingEach(itemList) { item ->
            database.accountChecklistItemSyncDao().clearPending(
                accountId = accountId,
                itemId = item.id,
                updatedAt = item.updatedAt,
            )
        }
    }

    override suspend fun upsert(
        accountId: Uuid,
        itemList: List<ChecklistItemLocalEntity>,
        cursor: Long,
    ) {
        database.upsertPulled(
            accountId = accountId,
            kind = SyncKindLocalEntity.CHECKLIST_ITEM,
            cursor = cursor,
            pulledList = itemList,
            keyOf = { item -> item.id },
            updatedAtOf = { item -> item.updatedAt },
            readLocalUpdatedAtMap = { pulledList -> database.checklistItemDao().findUpdatedAt(pulledList.map { item -> item.id }) },
            upsert = { upsertList -> database.checklistItemDao().upsert(upsertList) },
            insertIgnoreAccount = { pulledList ->
                database.accountChecklistItemSyncDao().insertIgnore(
                    pulledList.map { item ->
                        AccountChecklistItemLocalEntity(
                            accountId = accountId,
                            checklistItemId = item.id,
                            isDirty = false,
                        )
                    },
                )
            },
        )
    }
}
