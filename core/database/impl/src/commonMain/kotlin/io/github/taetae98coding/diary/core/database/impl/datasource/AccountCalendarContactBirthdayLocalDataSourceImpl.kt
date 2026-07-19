package io.github.taetae98coding.diary.core.database.impl.datasource

import io.github.taetae98coding.diary.core.database.api.contact.datasource.AccountCalendarContactBirthdayLocalDataSource
import io.github.taetae98coding.diary.core.database.api.contact.entity.CalendarContactBirthdayLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateRange
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountCalendarContactBirthdayLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountCalendarContactBirthdayLocalDataSource {
    override fun get(
        accountId: Uuid,
        dateRange: LocalDateRange,
    ): Flow<List<CalendarContactBirthdayLocalEntity>> =
        // DAO는 기간 타입을 바인딩할 수 없어 시작일과 종료일로 나눠 넘긴다.
        database.accountCalendarContactBirthdayDao().get(
            accountId = accountId,
            start = dateRange.start,
            endInclusive = dateRange.endInclusive,
        )
}
