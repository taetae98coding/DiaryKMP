package io.github.taetae98coding.diary.core.database.impl.search.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineCurrentCycleLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineCycleLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineSummaryLocalEntity
import io.github.taetae98coding.diary.core.database.api.search.datasource.SearchRoutineLocalDataSource
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class SearchRoutineLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : SearchRoutineLocalDataSource {
    override fun page(
        accountId: Uuid,
        query: String,
        currentCycle: RoutineCurrentCycleLocalEntity,
    ): PagingSource<Int, RoutineSummaryLocalEntity> =
        database.searchRoutineDao().page(
            accountId = accountId,
            query = query,
            today = currentCycle.today,
            weekStart = currentCycle.week.start,
            weekEndInclusive = currentCycle.week.endInclusive,
            monthStart = currentCycle.month.start,
            monthEndInclusive = currentCycle.month.endInclusive,
            weeklyCycle = RoutineCycleLocalEntity.WEEKLY,
            monthlyCycle = RoutineCycleLocalEntity.MONTHLY,
        )
}
