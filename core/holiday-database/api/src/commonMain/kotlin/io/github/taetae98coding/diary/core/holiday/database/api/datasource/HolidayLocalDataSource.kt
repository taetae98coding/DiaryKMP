package io.github.taetae98coding.diary.core.holiday.database.api.datasource

import io.github.taetae98coding.diary.core.holiday.database.api.entity.HolidayLocalEntity
import kotlinx.coroutines.flow.Flow

public interface HolidayLocalDataSource {
    public fun get(): Flow<List<HolidayLocalEntity>>

    public fun get(year: Int): Flow<List<HolidayLocalEntity>>
}
