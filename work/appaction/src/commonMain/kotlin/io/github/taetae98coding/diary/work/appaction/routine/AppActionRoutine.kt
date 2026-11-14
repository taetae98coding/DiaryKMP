package io.github.taetae98coding.diary.work.appaction.routine

import io.github.taetae98coding.diary.core.model.routine.RoutineProgress
import io.github.taetae98coding.diary.core.model.routine.RoutineRecord
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.domain.routine.progress.achievementRate
import io.github.taetae98coding.diary.domain.routine.progress.hasCycleProgress
import io.github.taetae98coding.diary.domain.routine.progress.progress
import io.github.taetae98coding.diary.work.appaction.core.toColorHex
import io.github.taetae98coding.diary.work.appaction.tag.AppActionTag
import kotlinx.datetime.LocalDate

public object AppActionRoutineStatusValue {
    public const val NOT_STARTED: String = "not_started"
    public const val IN_PROGRESS: String = "in_progress"
}

public data class AppActionRoutineProgress(
    val status: String,
    val cycleAmount: Long?,
    val achievementRate: Int?,
)

public data class AppActionRoutine(
    val id: String,
    val title: String,
    val description: String,
    val color: String,
    val startDate: String,
    val cycle: String,
    val unit: String,
    val goalAmount: Int?,
    val amountPerRecord: Int,
    val isFinished: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val progress: AppActionRoutineProgress,
)

public data class AppActionRoutineDetail(
    val routine: AppActionRoutine,
    val tags: List<AppActionTag>,
)

public data class AppActionRoutineRecord(
    val id: String,
    val routineId: String,
    val date: String,
    val amount: Int,
    val createdAt: String,
    val updatedAt: String,
)

// 데스크톱 MCP 연결도 이 값으로 결과를 만들어 두 경로의 루틴 결과가 같게 한다.
public fun RoutineSummary.toAppActionRoutine(today: LocalDate): AppActionRoutine {
    val detail = routine.detail
    val hasCycleProgress = hasCycleProgress(today = today)

    return AppActionRoutine(
        id = routine.id.toString(),
        title = detail.title,
        description = detail.description,
        color = detail.color.toColorHex(),
        startDate = detail.startDate.toString(),
        cycle = detail.cycle.toRoutineCycleValue(),
        unit = detail.unit,
        goalAmount = detail.goalAmount,
        amountPerRecord = detail.amountPerRecord,
        isFinished = routine.isFinished,
        createdAt = routine.createdAt.toString(),
        updatedAt = routine.updatedAt.toString(),
        progress =
            AppActionRoutineProgress(
                status = detail.progress(today = today).toRoutineStatusValue(),
                cycleAmount = cycleAmount.takeIf { hasCycleProgress },
                achievementRate = if (hasCycleProgress) detail.achievementRate(cycleAmount = cycleAmount) else null,
            ),
    )
}

public fun RoutineRecord.toAppActionRoutineRecord(): AppActionRoutineRecord =
    AppActionRoutineRecord(
        id = id.toString(),
        routineId = routineId.toString(),
        date = date.toString(),
        amount = amount,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
    )

private fun RoutineProgress.toRoutineStatusValue(): String =
    when (this) {
        RoutineProgress.NOT_STARTED -> AppActionRoutineStatusValue.NOT_STARTED
        RoutineProgress.IN_PROGRESS -> AppActionRoutineStatusValue.IN_PROGRESS
    }
