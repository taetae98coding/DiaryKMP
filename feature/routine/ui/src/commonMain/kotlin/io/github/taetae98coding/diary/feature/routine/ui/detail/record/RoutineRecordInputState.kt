package io.github.taetae98coding.diary.feature.routine.ui.detail.record

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.core.model.routine.RoutineDetail
import io.github.taetae98coding.diary.core.model.routine.RoutineProgress
import io.github.taetae98coding.diary.domain.routine.progress.progress
import io.github.taetae98coding.diary.feature.routine.ui.form.RoutineAmountInputState
import io.github.taetae98coding.diary.feature.routine.ui.form.rememberRoutineAmountInputState
import io.github.taetae98coding.diary.feature.routine.ui.form.today
import kotlinx.datetime.LocalDate

@Stable
internal class RoutineRecordInputState(
    val dateState: RoutineRecordDateState,
    val amountState: RoutineAmountInputState,
    val datePickerDialogState: DialogState,
) {
    val date: LocalDate
        get() = dateState.date

    val isAddable: Boolean
        get() = amountState.isPositive
}

@Stable
internal class RoutineRecordDateState(
    date: LocalDate,
) {
    var date: LocalDate by mutableStateOf(date)
        private set

    fun select(date: LocalDate) {
        this.date = date
    }

    fun coerceAtLeast(startDate: LocalDate) {
        if (date < startDate) date = startDate
    }

    companion object {
        val Saver: Saver<RoutineRecordDateState, Long> =
            Saver(
                save = { state -> state.date.toEpochDays() },
                restore = { saved -> RoutineRecordDateState(date = LocalDate.fromEpochDays(saved)) },
            )
    }
}

@Composable
internal fun rememberRoutineRecordInputState(
    initialDate: LocalDate = remember { today() },
    initialAmount: Int? = null,
): RoutineRecordInputState {
    val dateState = rememberSaveable(saver = RoutineRecordDateState.Saver) { RoutineRecordDateState(date = initialDate) }
    val amountState = rememberRoutineAmountInputState(initialAmount = initialAmount)
    val datePickerDialogState = rememberDialogState()

    return remember(dateState, amountState, datePickerDialogState) {
        RoutineRecordInputState(
            dateState = dateState,
            amountState = amountState,
            datePickerDialogState = datePickerDialogState,
        )
    }
}

internal fun RoutineDetail.initialRecordDate(today: LocalDate): LocalDate =
    when (progress(today = today)) {
        RoutineProgress.IN_PROGRESS -> today
        RoutineProgress.NOT_STARTED -> startDate
    }
