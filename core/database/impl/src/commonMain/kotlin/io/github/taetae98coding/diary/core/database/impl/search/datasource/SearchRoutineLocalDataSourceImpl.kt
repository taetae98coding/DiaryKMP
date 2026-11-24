package io.github.taetae98coding.diary.core.database.impl.search.datasource

import androidx.paging.PagingSource
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineCurrentCycleLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineCycleLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineSummaryLocalEntity
import io.github.taetae98coding.diary.core.database.api.search.datasource.SearchRoutineLocalDataSource
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
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
