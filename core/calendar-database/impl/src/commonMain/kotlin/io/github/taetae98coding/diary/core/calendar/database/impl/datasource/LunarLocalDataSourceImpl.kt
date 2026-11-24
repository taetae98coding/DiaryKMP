package io.github.taetae98coding.diary.core.calendar.database.impl.datasource

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.calendar.database.api.datasource.LunarLocalDataSource
import io.github.taetae98coding.diary.core.calendar.database.api.entity.LunarDateLocalEntity
import io.github.taetae98coding.diary.core.calendar.database.impl.CalendarDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateRange

@Inject
@ContributesBinding(AppScope::class)
internal class LunarLocalDataSourceImpl(
    private val database: CalendarDatabase,
) : LunarLocalDataSource {
    override fun get(dateRange: LocalDateRange): Flow<List<LunarDateLocalEntity>> =
        database.lunarDateDao().get(
            start = dateRange.start,
            endInclusive = dateRange.endInclusive,
        )
}
