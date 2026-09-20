package io.github.taetae98coding.diary.core.database.api.memo.datasource

import io.github.taetae98coding.diary.core.database.api.memo.entity.DailyMemoLocalEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

public interface AccountDailyMemoLocalDataSource {
    public fun get(
        accountId: Uuid,
        date: LocalDate,
    ): Flow<List<DailyMemoLocalEntity>>
}
