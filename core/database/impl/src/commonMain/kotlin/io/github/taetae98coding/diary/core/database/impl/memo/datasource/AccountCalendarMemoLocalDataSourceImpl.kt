package io.github.taetae98coding.diary.core.database.impl.memo.datasource

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.database.api.memo.datasource.AccountCalendarMemoLocalDataSource
import io.github.taetae98coding.diary.core.database.api.memo.entity.CalendarMemoLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateRange
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
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
