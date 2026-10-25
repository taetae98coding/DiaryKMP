package io.github.taetae98coding.diary.core.database.api.checklist.transaction

import io.github.taetae98coding.diary.core.database.api.checklist.entity.ChecklistDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.checklist.entity.ChecklistLocalEntity
import io.github.taetae98coding.diary.core.database.api.checklistitem.entity.ChecklistItemLocalEntity
import kotlin.time.Instant
import kotlin.uuid.Uuid

public interface AccountChecklistTransaction {
    public suspend fun upsert(
        accountId: Uuid,
        checklist: ChecklistLocalEntity,
        itemList: List<ChecklistItemLocalEntity>,
    )

    public suspend fun updateDetail(
        accountId: Uuid,
        checklistId: Uuid,
        detail: ChecklistDetailLocalEntity,
        updatedAt: Instant,
    ): Int

    public suspend fun updateDeleted(
        accountId: Uuid,
        checklistId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int
}
