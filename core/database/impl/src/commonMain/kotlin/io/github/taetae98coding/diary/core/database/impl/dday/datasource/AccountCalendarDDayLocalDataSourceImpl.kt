package io.github.taetae98coding.diary.core.database.impl.dday.datasource

import io.github.taetae98coding.diary.core.database.api.dday.datasource.AccountCalendarDDayLocalDataSource
import io.github.taetae98coding.diary.core.database.api.dday.entity.CalendarDDayKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.dday.entity.CalendarDDayLocalEntity
import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayOverflowPolicyLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateRange
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountCalendarDDayLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountCalendarDDayLocalDataSource {
    override fun get(
        accountId: Uuid,
        dateRange: LocalDateRange,
    ): Flow<List<CalendarDDayLocalEntity>> =
        database.accountCalendarDDayDao().get(
            accountId = accountId,
            start = dateRange.start,
            endInclusive = dateRange.endInclusive,
            clampToMonthEnd = DDayOverflowPolicyLocalEntity.CLAMP_TO_MONTH_END,
            nextMonthFirstDay = DDayOverflowPolicyLocalEntity.NEXT_MONTH_FIRST_DAY,
            baseDayKind = CalendarDDayKindLocalEntity.BASE_DAY,
            yearsKind = CalendarDDayKindLocalEntity.YEARS,
            monthsKind = CalendarDDayKindLocalEntity.MONTHS,
            daysKind = CalendarDDayKindLocalEntity.DAYS,
        )
}
