package io.github.taetae98coding.diary.core.database.api.checklistitem.transaction

import io.github.taetae98coding.diary.core.database.api.checklistitem.entity.ChecklistItemLocalEntity
import kotlin.time.Instant
import kotlin.uuid.Uuid

public interface AccountChecklistItemTransaction {
    public suspend fun upsert(
        accountId: Uuid,
        item: ChecklistItemLocalEntity,
    ): Int

    public suspend fun updateChecked(
        accountId: Uuid,
        itemId: Uuid,
        isChecked: Boolean,
        updatedAt: Instant,
    ): Int

    public suspend fun updateDeleted(
        accountId: Uuid,
        itemId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int

    public suspend fun updateAllChecked(
        accountId: Uuid,
        checklistId: Uuid,
        isChecked: Boolean,
        updatedAt: Instant,
    ): Int
}
