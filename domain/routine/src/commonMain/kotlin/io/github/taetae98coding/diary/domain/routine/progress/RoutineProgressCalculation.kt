package io.github.taetae98coding.diary.domain.routine.progress

import io.github.taetae98coding.diary.core.model.routine.RoutineDetail
import io.github.taetae98coding.diary.core.model.routine.RoutineProgress
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import kotlinx.datetime.LocalDate

private const val PERCENT: Long = 100L

public fun RoutineDetail.progress(today: LocalDate): RoutineProgress = if (today < startDate) RoutineProgress.NOT_STARTED else RoutineProgress.IN_PROGRESS

public fun RoutineDetail.canRecord(date: LocalDate): Boolean = date >= startDate

public fun RoutineDetail.achievementRate(cycleAmount: Long): Int? {
    val goalAmount = goalAmount ?: return null

    return (cycleAmount * PERCENT / goalAmount).coerceAtMost(PERCENT).toInt()
}

public fun RoutineSummary.hasCycleProgress(today: LocalDate): Boolean = !routine.isFinished && routine.detail.progress(today = today) == RoutineProgress.IN_PROGRESS
