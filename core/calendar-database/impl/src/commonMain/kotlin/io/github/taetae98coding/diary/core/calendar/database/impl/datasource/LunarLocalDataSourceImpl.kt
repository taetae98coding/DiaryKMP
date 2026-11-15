package io.github.taetae98coding.diary.core.calendar.database.impl.datasource

import io.github.taetae98coding.diary.core.calendar.database.api.datasource.LunarLocalDataSource
import io.github.taetae98coding.diary.core.calendar.database.api.entity.LunarDateLocalEntity
import io.github.taetae98coding.diary.core.calendar.database.impl.CalendarDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateRange
import org.koin.core.annotation.Factory

@Factory
internal class LunarLocalDataSourceImpl(
    private val database: CalendarDatabase,
) : LunarLocalDataSource {
    override fun get(dateRange: LocalDateRange): Flow<List<LunarDateLocalEntity>> =
        database.lunarDateDao().get(
            start = dateRange.start,
            endInclusive = dateRange.endInclusive,
        )
}
