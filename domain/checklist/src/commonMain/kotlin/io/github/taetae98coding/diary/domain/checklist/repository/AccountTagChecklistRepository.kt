package io.github.taetae98coding.diary.domain.checklist.repository

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.checklist.ChecklistSummary
import io.github.taetae98coding.diary.core.model.tag.TagScope
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

public interface AccountTagChecklistRepository {
    public fun page(
        account: Account,
        tagId: Uuid,
        scope: TagScope,
    ): Flow<PagingData<ChecklistSummary>>
}
