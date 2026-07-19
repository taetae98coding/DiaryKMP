package io.github.taetae98coding.diary.core.database.impl.datasource

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
        // DAO는 기간 타입을 바인딩할 수 없어 시작일과 종료일로 나눠 넘긴다.
        database.accountCalendarMemoDao().get(
            accountId = accountId,
            start = dateRange.start,
            endInclusive = dateRange.endInclusive,
        )
}
