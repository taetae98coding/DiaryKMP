package io.github.taetae98coding.diary.core.network.api.checklistitem.datasource

import io.github.taetae98coding.diary.core.network.api.checklistitem.entity.ChecklistItemPullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.checklistitem.entity.ChecklistItemRemoteEntity

public interface ChecklistItemRemoteDataSource {
    public suspend fun push(checklistItemList: List<ChecklistItemRemoteEntity>)

    public suspend fun pull(usn: Long): List<ChecklistItemPullRemoteEntity>
}
