package io.github.taetae98coding.diary.core.database.impl.routine.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.routine.datasource.AccountRoutineLocalDataSource
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineCurrentCycleLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineCycleLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineSummaryLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountRoutineLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountRoutineLocalDataSource {
    override fun page(
        accountId: Uuid,
        currentCycle: RoutineCurrentCycleLocalEntity,
    ): PagingSource<Int, RoutineSummaryLocalEntity> =
        database.accountRoutineDao().page(
            accountId = accountId,
            today = currentCycle.today,
            weekStart = currentCycle.week.start,
            weekEndInclusive = currentCycle.week.endInclusive,
            monthStart = currentCycle.month.start,
            monthEndInclusive = currentCycle.month.endInclusive,
            weeklyCycle = RoutineCycleLocalEntity.WEEKLY,
            monthlyCycle = RoutineCycleLocalEntity.MONTHLY,
            isStartedOnly = false,
            cycleList = RoutineCycleLocalEntity.entries.map { cycle -> cycle.persistentValue },
        )

    override fun pageStarted(
        accountId: Uuid,
        currentCycle: RoutineCurrentCycleLocalEntity,
        cycleList: List<RoutineCycleLocalEntity>,
    ): PagingSource<Int, RoutineSummaryLocalEntity> =
        database.accountRoutineDao().page(
            accountId = accountId,
            today = currentCycle.today,
            weekStart = currentCycle.week.start,
            weekEndInclusive = currentCycle.week.endInclusive,
            monthStart = currentCycle.month.start,
            monthEndInclusive = currentCycle.month.endInclusive,
            weeklyCycle = RoutineCycleLocalEntity.WEEKLY,
            monthlyCycle = RoutineCycleLocalEntity.MONTHLY,
            isStartedOnly = true,
            cycleList = cycleList.map { cycle -> cycle.persistentValue },
        )

    override fun pageFinished(
        accountId: Uuid,
        currentCycle: RoutineCurrentCycleLocalEntity,
    ): PagingSource<Int, RoutineSummaryLocalEntity> =
        database.accountRoutineDao().pageFinished(
            accountId = accountId,
            today = currentCycle.today,
            weekStart = currentCycle.week.start,
            weekEndInclusive = currentCycle.week.endInclusive,
            monthStart = currentCycle.month.start,
            monthEndInclusive = currentCycle.month.endInclusive,
            weeklyCycle = RoutineCycleLocalEntity.WEEKLY,
            monthlyCycle = RoutineCycleLocalEntity.MONTHLY,
        )

    override fun getUnfinishedExists(accountId: Uuid): Flow<Boolean> = database.accountRoutineDao().getUnfinishedExists(accountId = accountId)

    override fun find(
        accountId: Uuid,
        routineId: Uuid,
    ): Flow<RoutineLocalEntity?> =
        database.accountRoutineDao().find(
            accountId = accountId,
            routineId = routineId,
        )

    override fun findSummary(
        accountId: Uuid,
        routineId: Uuid,
        currentCycle: RoutineCurrentCycleLocalEntity,
    ): Flow<RoutineSummaryLocalEntity?> =
        database.accountRoutineDao().findSummary(
            accountId = accountId,
            routineId = routineId,
            today = currentCycle.today,
            weekStart = currentCycle.week.start,
            weekEndInclusive = currentCycle.week.endInclusive,
            monthStart = currentCycle.month.start,
            monthEndInclusive = currentCycle.month.endInclusive,
            weeklyCycle = RoutineCycleLocalEntity.WEEKLY,
            monthlyCycle = RoutineCycleLocalEntity.MONTHLY,
        )
}
