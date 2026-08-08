package io.github.taetae98coding.diary.domain.holiday.repository

import io.github.taetae98coding.diary.core.model.holiday.Holiday
import kotlinx.coroutines.flow.Flow

public interface HolidayRepository {
    public suspend fun fetch(year: Int): List<Holiday>

    public fun get(): Flow<List<Holiday>>

    public fun get(year: Int): Flow<List<Holiday>>
}
