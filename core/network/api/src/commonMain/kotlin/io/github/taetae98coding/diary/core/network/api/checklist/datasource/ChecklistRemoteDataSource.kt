package io.github.taetae98coding.diary.core.network.api.checklist.datasource

import io.github.taetae98coding.diary.core.network.api.checklist.entity.ChecklistPullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.checklist.entity.ChecklistRemoteEntity

public interface ChecklistRemoteDataSource {
    public suspend fun push(checklistList: List<ChecklistRemoteEntity>)

    public suspend fun pull(usn: Long): List<ChecklistPullRemoteEntity>
}
