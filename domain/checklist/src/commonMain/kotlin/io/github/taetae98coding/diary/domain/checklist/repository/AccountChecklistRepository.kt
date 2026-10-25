package io.github.taetae98coding.diary.domain.checklist.repository

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.checklist.Checklist
import io.github.taetae98coding.diary.core.model.checklist.ChecklistDetail
import io.github.taetae98coding.diary.core.model.checklist.ChecklistItem
import io.github.taetae98coding.diary.core.model.checklist.ChecklistSummary
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant
import kotlin.uuid.Uuid

public interface AccountChecklistRepository {
    public fun page(account: Account): Flow<PagingData<ChecklistSummary>>

    public fun find(
        account: Account,
        checklistId: Uuid,
    ): Flow<Checklist?>

    public suspend fun upsert(
        account: Account,
        checklist: Checklist,
        itemList: List<ChecklistItem>,
    )

    public suspend fun updateDetail(
        account: Account,
        checklistId: Uuid,
        detail: ChecklistDetail,
        updatedAt: Instant,
    ): Int

    public suspend fun updateDeleted(
        account: Account,
        checklistId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int
}
