package io.github.taetae98coding.diary.core.database.impl.checklist.transaction

import androidx.room3.withWriteTransaction
import io.github.taetae98coding.diary.core.database.api.checklist.entity.ChecklistDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.checklist.entity.ChecklistLocalEntity
import io.github.taetae98coding.diary.core.database.api.checklist.transaction.AccountChecklistTransaction
import io.github.taetae98coding.diary.core.database.api.checklistitem.entity.ChecklistItemLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.checklist.entity.AccountChecklistLocalEntity
import io.github.taetae98coding.diary.core.database.impl.checklistitem.entity.AccountChecklistItemLocalEntity
import org.koin.core.annotation.Factory
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Factory
internal class AccountChecklistTransactionImpl(
    private val database: DiaryDatabase,
) : AccountChecklistTransaction {
    override suspend fun upsert(
        accountId: Uuid,
        checklist: ChecklistLocalEntity,
        itemList: List<ChecklistItemLocalEntity>,
    ) {
        database.withWriteTransaction {
            database.checklistDao().upsert(checklist)
            database.accountChecklistDao().upsert(
                AccountChecklistLocalEntity(
                    accountId = accountId,
                    checklistId = checklist.id,
                    isDirty = true,
                ),
            )
            database.checklistItemDao().upsert(itemList)
            database.accountChecklistItemDao().upsert(
                itemList.map { item ->
                    AccountChecklistItemLocalEntity(
                        accountId = accountId,
                        checklistItemId = item.id,
                        isDirty = true,
                    )
                },
            )
        }
    }

    override suspend fun updateDetail(
        accountId: Uuid,
        checklistId: Uuid,
        detail: ChecklistDetailLocalEntity,
        updatedAt: Instant,
    ): Int =
        updateAndMarkPending(
            accountId = accountId,
            checklistId = checklistId,
        ) {
            database.accountChecklistDao().updateDetail(
                accountId = accountId,
                checklistId = checklistId,
                title = detail.title,
                description = detail.description,
                updatedAt = updatedAt,
            )
        }

    override suspend fun updateDeleted(
        accountId: Uuid,
        checklistId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int =
        updateAndMarkPending(
            accountId = accountId,
            checklistId = checklistId,
        ) {
            database.accountChecklistDao().updateDeleted(
                accountId = accountId,
                checklistId = checklistId,
                isDeleted = isDeleted,
                updatedAt = updatedAt,
            )
        }

    private suspend fun updateAndMarkPending(
        accountId: Uuid,
        checklistId: Uuid,
        update: suspend () -> Int,
    ): Int =
        database.withWriteTransaction {
            val updatedCount = update()

            if (updatedCount > 0) {
                database.accountChecklistDao().markPending(accountId = accountId, checklistId = checklistId)
            }

            updatedCount
        }
}
