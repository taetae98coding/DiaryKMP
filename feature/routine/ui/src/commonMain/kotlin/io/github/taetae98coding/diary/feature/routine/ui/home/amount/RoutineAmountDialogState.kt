package io.github.taetae98coding.diary.feature.routine.ui.home.amount

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import io.github.taetae98coding.diary.core.model.routine.Routine
import io.github.taetae98coding.diary.feature.routine.ui.home.record.RoutineHomeRecord
import kotlin.uuid.Uuid

@Stable
internal class RoutineAmountDialogState(
    target: RoutineAmountDialogTarget? = null,
    amount: String = "",
) {
    val amountState: TextFieldState = TextFieldState(initialText = amount)

    var target: RoutineAmountDialogTarget? by mutableStateOf(target)
        private set

    val isVisible: Boolean
        get() = target != null

    val record: RoutineHomeRecord? by derivedStateOf {
        val target = this.target
        val amount = amountState.text.toString().toIntOrNull()

        if (target == null || amount == null || amount <= 0) {
            null
        } else {
            RoutineHomeRecord(routineId = target.routineId, title = target.title, unit = target.unit, amount = amount)
        }
    }

    fun show(routine: Routine) {
        target = RoutineAmountDialogTarget(routineId = routine.id, title = routine.detail.title, unit = routine.detail.unit)
        amountState.edit {
            replace(0, length, routine.detail.amountPerRecord.toString())
            selection = TextRange(0, length)
        }
    }

    fun hide() {
        target = null
    }

    companion object {
        private const val SAVED_FIELD_COUNT = 4
        private const val ROUTINE_ID_INDEX = 0
        private const val TITLE_INDEX = 1
        private const val UNIT_INDEX = 2
        private const val AMOUNT_INDEX = 3

        val Saver: Saver<RoutineAmountDialogState, Any> =
            listSaver(
                save = { state ->
                    state.target
                        ?.let { target -> listOf(target.routineId.toString(), target.title, target.unit, state.amountState.text.toString()) }
                        .orEmpty()
                },
                restore = { saved ->
                    if (saved.size == SAVED_FIELD_COUNT) {
                        RoutineAmountDialogState(
                            target =
                                RoutineAmountDialogTarget(
                                    routineId = Uuid.parse(saved[ROUTINE_ID_INDEX]),
                                    title = saved[TITLE_INDEX],
                                    unit = saved[UNIT_INDEX],
                                ),
                            amount = saved[AMOUNT_INDEX],
                        )
                    } else {
                        RoutineAmountDialogState()
                    }
                },
            )
    }
}

internal data class RoutineAmountDialogTarget(
    val routineId: Uuid,
    val title: String,
    val unit: String,
)

@Composable
internal fun rememberRoutineAmountDialogState(): RoutineAmountDialogState =
    rememberSaveable(saver = RoutineAmountDialogState.Saver) {
        RoutineAmountDialogState()
    }
