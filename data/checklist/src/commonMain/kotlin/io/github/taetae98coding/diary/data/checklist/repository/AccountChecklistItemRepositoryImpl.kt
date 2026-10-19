package io.github.taetae98coding.diary.data.checklist.repository

import io.github.taetae98coding.diary.core.database.api.checklistitem.datasource.AccountChecklistItemLocalDataSource
import io.github.taetae98coding.diary.core.database.api.checklistitem.transaction.AccountChecklistItemTransaction
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.checklist.ChecklistItem
import io.github.taetae98coding.diary.core.model.checklist.ChecklistItemSort
import io.github.taetae98coding.diary.data.checklist.mapper.toDomain
import io.github.taetae98coding.diary.data.checklist.mapper.toLocal
import io.github.taetae98coding.diary.domain.checklist.repository.AccountChecklistItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Factory
internal class AccountChecklistItemRepositoryImpl(
    private val accountChecklistItemLocalDataSource: AccountChecklistItemLocalDataSource,
    private val accountChecklistItemTransaction: AccountChecklistItemTransaction,
) : AccountChecklistItemRepository {
    override fun get(
        account: Account,
        checklistId: Uuid,
        sort: ChecklistItemSort,
    ): Flow<List<ChecklistItem>> =
        accountChecklistItemLocalDataSource
            .get(
                accountId = account.id,
                checklistId = checklistId,
                sort = sort.toLocal(),
            ).map { localList -> localList.map { local -> local.toDomain() } }

    override suspend fun upsert(
        account: Account,
        item: ChecklistItem,
    ): Int =
        accountChecklistItemTransaction.upsert(
            accountId = account.id,
            item = item.toLocal(),
        )

    override suspend fun updateChecked(
        account: Account,
        itemId: Uuid,
        isChecked: Boolean,
        updatedAt: Instant,
    ): Int =
        accountChecklistItemTransaction.updateChecked(
            accountId = account.id,
            itemId = itemId,
            isChecked = isChecked,
            updatedAt = updatedAt,
        )

    override suspend fun updateDeleted(
        account: Account,
        itemId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int =
        accountChecklistItemTransaction.updateDeleted(
            accountId = account.id,
            itemId = itemId,
            isDeleted = isDeleted,
            updatedAt = updatedAt,
        )

    override suspend fun updateAllChecked(
        account: Account,
        checklistId: Uuid,
        isChecked: Boolean,
        updatedAt: Instant,
    ): Int =
        accountChecklistItemTransaction.updateAllChecked(
            accountId = account.id,
            checklistId = checklistId,
            isChecked = isChecked,
            updatedAt = updatedAt,
        )
}
