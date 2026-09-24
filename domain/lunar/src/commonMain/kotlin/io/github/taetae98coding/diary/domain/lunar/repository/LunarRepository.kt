package io.github.taetae98coding.diary.domain.lunar.repository

import io.github.taetae98coding.diary.core.model.lunar.LunarDate
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateRange

public interface LunarRepository {
    public suspend fun fetch(year: Int): List<LunarDate>

    public fun get(dateRange: LocalDateRange): Flow<List<LunarDate>>
}
