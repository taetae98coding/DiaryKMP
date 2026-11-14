package io.github.taetae98coding.diary.feature.routine.ui.home.record

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.compose.routine.routineAmountText
import io.github.taetae98coding.diary.feature.routine.ui.Res
import io.github.taetae98coding.diary.feature.routine.ui.routine_home_record_undo_action
import io.github.taetae98coding.diary.feature.routine.ui.routine_home_recorded_message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun RoutineHomeRecordUndoSnackbarEffect(
    onUndo: (Uuid) -> Unit,
    effect: Flow<RoutineHomeRecordEffect> = emptyFlow(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val coroutineScope = rememberCoroutineScope()
    val undoActionLabel = stringResource(Res.string.routine_home_record_undo_action)

    CollectEffect(effect) { value ->
        when (value) {
            is RoutineHomeRecordEffect.Recorded -> {
                val amountText = routineAmountText(amount = value.record.amount.toLong(), unit = value.record.unit)
                val message = getString(Res.string.routine_home_recorded_message, value.record.title, amountText)

                coroutineScope.launch {
                    val result = snackbarHostState.showImmediate(message = message, actionLabel = undoActionLabel)

                    if (result == SnackbarResult.ActionPerformed) {
                        onUndo(value.recordId)
                    }
                }
            }
        }
    }
}
