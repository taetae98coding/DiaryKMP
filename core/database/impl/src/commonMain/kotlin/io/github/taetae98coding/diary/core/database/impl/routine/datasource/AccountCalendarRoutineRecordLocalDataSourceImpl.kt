package io.github.taetae98coding.diary.core.database.impl.routine.datasource

import io.github.taetae98coding.diary.core.database.api.routine.datasource.AccountCalendarRoutineRecordLocalDataSource
import io.github.taetae98coding.diary.core.database.api.routine.entity.CalendarRoutineRecordLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateRange
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountCalendarRoutineRecordLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountCalendarRoutineRecordLocalDataSource {
    override fun get(
        accountId: Uuid,
        dateRange: LocalDateRange,
    ): Flow<List<CalendarRoutineRecordLocalEntity>> =
        database.accountCalendarRoutineRecordDao().get(
            accountId = accountId,
            start = dateRange.start,
            endInclusive = dateRange.endInclusive,
        )
}
