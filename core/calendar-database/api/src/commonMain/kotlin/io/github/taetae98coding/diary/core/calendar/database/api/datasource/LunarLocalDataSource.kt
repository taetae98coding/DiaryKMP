package io.github.taetae98coding.diary.core.calendar.database.api.datasource

import io.github.taetae98coding.diary.core.calendar.database.api.entity.LunarDateLocalEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateRange

public interface LunarLocalDataSource {
    public fun get(dateRange: LocalDateRange): Flow<List<LunarDateLocalEntity>>
}
