package io.github.taetae98coding.diary.compose.routine

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import io.github.taetae98coding.diary.core.model.routine.RoutineCycle
import io.github.taetae98coding.diary.core.model.routine.RoutineProgress
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.domain.routine.progress.achievementRate
import io.github.taetae98coding.diary.domain.routine.progress.hasCycleProgress
import io.github.taetae98coding.diary.domain.routine.progress.progress
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

@Immutable
public data class RoutineCardText(
    val isFinished: Boolean,
    val progress: RoutineProgress,
    val text: String,
    val achievementRate: Int?,
    val achievementRateText: String?,
) {
    val isInProgress: Boolean
        get() = !isFinished && progress == RoutineProgress.IN_PROGRESS
}

@Composable
public fun RoutineSummary.toCardText(today: LocalDate): RoutineCardText {
    val detail = routine.detail
    val progress = detail.progress(today = today)

    if (!hasCycleProgress(today = today)) {
        val text = if (routine.isFinished) Res.string.routine_card_finished else Res.string.routine_card_not_started

        return RoutineCardText(isFinished = routine.isFinished, progress = progress, text = stringResource(text), achievementRate = null, achievementRateText = null)
    }

    val cycleName = detail.cycle.toCycleName()
    val goalAmount = detail.goalAmount
    val achievementRate = detail.achievementRate(cycleAmount = cycleAmount)

    return RoutineCardText(
        isFinished = false,
        progress = progress,
        text =
            if (goalAmount == null) {
                stringResource(Res.string.routine_card_progress, cycleName, routineAmountText(amount = cycleAmount, unit = detail.unit))
            } else {
                stringResource(Res.string.routine_card_progress_with_goal, cycleName, cycleAmount.toString(), routineAmountText(amount = goalAmount.toLong(), unit = detail.unit))
            },
        achievementRate = achievementRate,
        achievementRateText = achievementRate?.let { rate -> stringResource(Res.string.routine_card_achievement_rate, rate.toString()) },
    )
}

@Composable
private fun RoutineCycle.toCycleName(): String =
    when (this) {
        RoutineCycle.DAILY, RoutineCycle.WEEKDAYS, RoutineCycle.WEEKENDS -> stringResource(Res.string.routine_card_cycle_daily)
        RoutineCycle.WEEKLY -> stringResource(Res.string.routine_card_cycle_weekly)
        RoutineCycle.MONTHLY -> stringResource(Res.string.routine_card_cycle_monthly)
    }
