package io.github.taetae98coding.diary.core.database.impl.checklist.transaction

import io.github.taetae98coding.diary.core.database.api.checklist.entity.ChecklistLocalEntity
import io.github.taetae98coding.diary.core.database.api.checklist.transaction.AccountChecklistSyncTransaction
import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.checklist.entity.AccountChecklistLocalEntity
import io.github.taetae98coding.diary.core.database.impl.sync.transaction.clearPendingEach
import io.github.taetae98coding.diary.core.database.impl.sync.transaction.upsertPulled
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountChecklistSyncTransactionImpl(
    private val database: DiaryDatabase,
) : AccountChecklistSyncTransaction {
    override suspend fun clearPending(
        accountId: Uuid,
        checklistList: List<ChecklistLocalEntity>,
    ) {
        database.clearPendingEach(checklistList) { checklist ->
            database.accountChecklistSyncDao().clearPending(
                accountId = accountId,
                checklistId = checklist.id,
                updatedAt = checklist.updatedAt,
            )
        }
    }

    override suspend fun upsert(
        accountId: Uuid,
        checklistList: List<ChecklistLocalEntity>,
        cursor: Long,
    ) {
        database.upsertPulled(
            accountId = accountId,
            kind = SyncKindLocalEntity.CHECKLIST,
            cursor = cursor,
            pulledList = checklistList,
            keyOf = { checklist -> checklist.id },
            updatedAtOf = { checklist -> checklist.updatedAt },
            readLocalUpdatedAtMap = { pulledList -> database.checklistDao().findUpdatedAt(pulledList.map { checklist -> checklist.id }) },
            upsert = { upsertList -> database.checklistDao().upsert(upsertList) },
            insertIgnoreAccount = { pulledList ->
                database.accountChecklistSyncDao().insertIgnore(
                    pulledList.map { checklist ->
                        AccountChecklistLocalEntity(
                            accountId = accountId,
                            checklistId = checklist.id,
                            isDirty = false,
                        )
                    },
                )
            },
        )
    }
}
