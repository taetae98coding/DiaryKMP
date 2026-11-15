package io.github.taetae98coding.diary.core.database.impl.memo.datasource

import io.github.taetae98coding.diary.core.database.api.memo.datasource.AccountCalendarMemoLocalDataSource
import io.github.taetae98coding.diary.core.database.api.memo.entity.CalendarMemoLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateRange
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountCalendarMemoLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountCalendarMemoLocalDataSource {
    override fun get(
        accountId: Uuid,
        dateRange: LocalDateRange,
    ): Flow<List<CalendarMemoLocalEntity>> =
        database.accountCalendarMemoDao().get(
            accountId = accountId,
            start = dateRange.start,
            endInclusive = dateRange.endInclusive,
        )
}
