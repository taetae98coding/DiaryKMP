package io.github.taetae98coding.diary.feature.routine.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.feature.routine.ui.Res
import io.github.taetae98coding.diary.feature.routine.ui.detail.record.RoutineDetailRecordEffect
import io.github.taetae98coding.diary.feature.routine.ui.form.RoutineAmountInputState
import io.github.taetae98coding.diary.feature.routine.ui.routine_amount_per_record_invalid_message
import io.github.taetae98coding.diary.feature.routine.ui.routine_detail_update_succeeded_message
import io.github.taetae98coding.diary.feature.routine.ui.routine_goal_amount_invalid_message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun RoutineDetailScreenEffect(
    navigateUp: () -> Unit,
    effect: Flow<RoutineDetailEffect> = emptyFlow(),
    state: RoutineDetailScaffoldState = rememberRoutineDetailScaffoldState(),
) {
    val coroutineScope = rememberCoroutineScope()
    val updateSucceededMessage = stringResource(Res.string.routine_detail_update_succeeded_message)
    val goalAmountInvalidMessage = stringResource(Res.string.routine_goal_amount_invalid_message)
    val amountPerRecordInvalidMessage = stringResource(Res.string.routine_amount_per_record_invalid_message)

    CollectEffect(effect) { value ->
        when (value) {
            is RoutineDetailEffect.UpdateSucceeded -> {
                coroutineScope.launch { state.formState.snackbarHostState.showImmediate(message = updateSucceededMessage) }
            }

            is RoutineDetailEffect.GoalAmountInvalid -> {
                coroutineScope.showAmountInvalid(state = state, amountState = state.formState.goalAmountState, message = goalAmountInvalidMessage)
            }

            is RoutineDetailEffect.AmountPerRecordInvalid -> {
                coroutineScope.showAmountInvalid(state = state, amountState = state.formState.amountPerRecordState, message = amountPerRecordInvalidMessage)
            }

            is RoutineDetailEffect.DeleteSucceeded -> {
                navigateUp()
            }
        }
    }
}

@Composable
internal fun RoutineDetailRecordAddEffect(
    effect: Flow<RoutineDetailRecordEffect> = emptyFlow(),
    state: RoutineDetailScaffoldState = rememberRoutineDetailScaffoldState(),
    uiStateProvider: () -> RoutineDetailUiState = { RoutineDetailUiState.Loading },
) {
    CollectEffect(effect) { value ->
        when (value) {
            is RoutineDetailRecordEffect.AddSucceeded -> {
                val content = uiStateProvider() as? RoutineDetailUiState.Content

                state.recordInputState.amountState.setAmount(amount = content?.detail?.amountPerRecord)
            }
        }
    }
}

@Composable
internal fun RoutineDetailRecordDateRangeEffect(
    state: RoutineDetailScaffoldState = rememberRoutineDetailScaffoldState(),
    uiStateProvider: () -> RoutineDetailUiState = { RoutineDetailUiState.Loading },
) {
    val latestUiStateProvider by rememberUpdatedState(uiStateProvider)

    LaunchedEffect(state) {
        snapshotFlow { (latestUiStateProvider() as? RoutineDetailUiState.Content)?.detail?.startDate }
            .filterNotNull()
            .collect { startDate -> state.recordInputState.dateState.coerceAtLeast(startDate = startDate) }
    }
}

private fun CoroutineScope.showAmountInvalid(
    state: RoutineDetailScaffoldState,
    amountState: RoutineAmountInputState,
    message: String,
) {
    launch { state.focusAmount(amountState = amountState) }
    launch { state.formState.snackbarHostState.showImmediate(message = message) }
}
