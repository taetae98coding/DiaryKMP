package io.github.taetae98coding.diary.core.database.api.dday.datasource

import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayLocalEntity
import kotlin.uuid.Uuid

public interface AccountDDaySyncLocalDataSource {
    public suspend fun readPendingList(accountId: Uuid): List<DDayLocalEntity>
}
