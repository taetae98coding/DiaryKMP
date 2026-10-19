package io.github.taetae98coding.diary.core.database.impl.checklistitem.transaction

import androidx.room3.withWriteTransaction
import io.github.taetae98coding.diary.core.database.api.checklistitem.entity.ChecklistItemLocalEntity
import io.github.taetae98coding.diary.core.database.api.checklistitem.transaction.AccountChecklistItemTransaction
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.checklistitem.entity.AccountChecklistItemLocalEntity
import org.koin.core.annotation.Factory
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Factory
internal class AccountChecklistItemTransactionImpl(
    private val database: DiaryDatabase,
) : AccountChecklistItemTransaction {
    override suspend fun upsert(
        accountId: Uuid,
        item: ChecklistItemLocalEntity,
    ): Int =
        database.withWriteTransaction {
            val isChecklistLinked =
                database.accountChecklistDao().exists(
                    accountId = accountId,
                    checklistId = item.checklistId,
                )

            if (!isChecklistLinked) return@withWriteTransaction 0

            database.checklistItemDao().upsert(item)
            database.accountChecklistItemDao().upsert(
                AccountChecklistItemLocalEntity(
                    accountId = accountId,
                    checklistItemId = item.id,
                    isDirty = true,
                ),
            )

            1
        }

    override suspend fun updateChecked(
        accountId: Uuid,
        itemId: Uuid,
        isChecked: Boolean,
        updatedAt: Instant,
    ): Int =
        updateAndMarkPending(
            accountId = accountId,
            itemId = itemId,
        ) {
            database.accountChecklistItemDao().updateChecked(
                accountId = accountId,
                itemId = itemId,
                isChecked = isChecked,
                updatedAt = updatedAt,
            )
        }

    override suspend fun updateDeleted(
        accountId: Uuid,
        itemId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int =
        updateAndMarkPending(
            accountId = accountId,
            itemId = itemId,
        ) {
            database.accountChecklistItemDao().updateDeleted(
                accountId = accountId,
                itemId = itemId,
                isDeleted = isDeleted,
                updatedAt = updatedAt,
            )
        }

    override suspend fun updateAllChecked(
        accountId: Uuid,
        checklistId: Uuid,
        isChecked: Boolean,
        updatedAt: Instant,
    ): Int =
        database.withWriteTransaction {
            val dao = database.accountChecklistItemDao()
            val itemIdList =
                dao.findIdListToChange(
                    accountId = accountId,
                    checklistId = checklistId,
                    isChecked = isChecked,
                )

            if (itemIdList.isEmpty()) return@withWriteTransaction 0

            dao.updateChecked(itemIdList = itemIdList, isChecked = isChecked, updatedAt = updatedAt)
            dao.markPending(accountId = accountId, itemIdList = itemIdList)

            itemIdList.size
        }

    private suspend fun updateAndMarkPending(
        accountId: Uuid,
        itemId: Uuid,
        update: suspend () -> Int,
    ): Int =
        database.withWriteTransaction {
            val updatedCount = update()

            if (updatedCount > 0) {
                database.accountChecklistItemDao().markPending(accountId = accountId, itemIdList = listOf(itemId))
            }

            updatedCount
        }
}
