package io.github.taetae98coding.diary.core.database.impl.routine.datasource

import androidx.paging.PagingSource
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.database.api.routine.datasource.AccountTagRoutineLocalDataSource
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineCurrentCycleLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineCycleLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineSummaryLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagScopeLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
internal class AccountTagRoutineLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountTagRoutineLocalDataSource {
    override fun page(
        accountId: Uuid,
        tagId: Uuid,
        scope: TagScopeLocalEntity,
        currentCycle: RoutineCurrentCycleLocalEntity,
    ): PagingSource<Int, RoutineSummaryLocalEntity> =
        database.accountTagRoutineDao().page(
            accountId = accountId,
            tagId = tagId,
            scope = scope.queryValue,
            today = currentCycle.today,
            weekStart = currentCycle.week.start,
            weekEndInclusive = currentCycle.week.endInclusive,
            monthStart = currentCycle.month.start,
            monthEndInclusive = currentCycle.month.endInclusive,
            weeklyCycle = RoutineCycleLocalEntity.WEEKLY,
            monthlyCycle = RoutineCycleLocalEntity.MONTHLY,
        )

    override fun pageFinished(
        accountId: Uuid,
        tagId: Uuid,
        currentCycle: RoutineCurrentCycleLocalEntity,
    ): PagingSource<Int, RoutineSummaryLocalEntity> =
        database.accountTagRoutineDao().pageFinished(
            accountId = accountId,
            tagId = tagId,
            today = currentCycle.today,
            weekStart = currentCycle.week.start,
            weekEndInclusive = currentCycle.week.endInclusive,
            monthStart = currentCycle.month.start,
            monthEndInclusive = currentCycle.month.endInclusive,
            weeklyCycle = RoutineCycleLocalEntity.WEEKLY,
            monthlyCycle = RoutineCycleLocalEntity.MONTHLY,
        )
}
