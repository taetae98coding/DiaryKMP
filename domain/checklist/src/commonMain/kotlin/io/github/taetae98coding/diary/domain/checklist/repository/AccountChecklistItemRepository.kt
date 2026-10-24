package io.github.taetae98coding.diary.domain.checklist.repository

import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.checklist.ChecklistItem
import io.github.taetae98coding.diary.core.model.checklist.ChecklistItemSort
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant
import kotlin.uuid.Uuid

public interface AccountChecklistItemRepository {
    public fun get(
        account: Account,
        checklistId: Uuid,
        sort: ChecklistItemSort,
    ): Flow<List<ChecklistItem>>

    public suspend fun upsert(
        account: Account,
        item: ChecklistItem,
    ): Int

    public suspend fun updateChecked(
        account: Account,
        itemId: Uuid,
        isChecked: Boolean,
        updatedAt: Instant,
    ): Int

    public suspend fun updateDeleted(
        account: Account,
        itemId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int

    public suspend fun updateAllChecked(
        account: Account,
        checklistId: Uuid,
        isChecked: Boolean,
        updatedAt: Instant,
    ): Int
}
