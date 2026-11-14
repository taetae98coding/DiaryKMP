package io.github.taetae98coding.diary.feature.routine.ui.add

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import io.github.taetae98coding.diary.compose.core.input.showDiaryFormAdded
import io.github.taetae98coding.diary.compose.core.input.showDiaryFormTitleBlank
import io.github.taetae98coding.diary.feature.routine.ui.Res
import io.github.taetae98coding.diary.feature.routine.ui.form.RoutineFormState
import io.github.taetae98coding.diary.feature.routine.ui.form.rememberRoutineAddFormState
import io.github.taetae98coding.diary.feature.routine.ui.form.showRoutineAmountInvalid
import io.github.taetae98coding.diary.feature.routine.ui.form.today
import io.github.taetae98coding.diary.feature.routine.ui.routine_add_succeeded_message
import io.github.taetae98coding.diary.feature.routine.ui.routine_add_title_blank_message
import io.github.taetae98coding.diary.feature.routine.ui.routine_amount_per_record_invalid_message
import io.github.taetae98coding.diary.feature.routine.ui.routine_goal_amount_invalid_message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun RoutineAddScreenEffect(
    effect: Flow<RoutineAddEffect> = emptyFlow(),
    state: RoutineFormState = rememberRoutineAddFormState(),
    scrollState: ScrollState = rememberScrollState(),
) {
    val coroutineScope = rememberCoroutineScope()
    val addSucceededMessage = stringResource(Res.string.routine_add_succeeded_message)
    val titleBlankMessage = stringResource(Res.string.routine_add_title_blank_message)
    val goalAmountInvalidMessage = stringResource(Res.string.routine_goal_amount_invalid_message)
    val amountPerRecordInvalidMessage = stringResource(Res.string.routine_amount_per_record_invalid_message)

    CollectEffect(effect) { value ->
        when (value) {
            is RoutineAddEffect.AddSucceeded -> {
                state.reset(startDate = today())
                coroutineScope.launch { state.descriptionState.reset() }
                scrollState.scrollTo(0)
                coroutineScope.showDiaryFormAdded(
                    titleState = state.titleState,
                    colorState = state.colorState,
                    snackbarHostState = state.snackbarHostState,
                    message = addSucceededMessage,
                )
            }

            is RoutineAddEffect.TitleBlank -> {
                scrollState.scrollTo(0)
                coroutineScope.showDiaryFormTitleBlank(
                    titleState = state.titleState,
                    snackbarHostState = state.snackbarHostState,
                    message = titleBlankMessage,
                )
            }

            is RoutineAddEffect.GoalAmountInvalid -> {
                coroutineScope.showRoutineAmountInvalid(
                    amountState = state.goalAmountState,
                    snackbarHostState = state.snackbarHostState,
                    message = goalAmountInvalidMessage,
                )
            }

            is RoutineAddEffect.AmountPerRecordInvalid -> {
                coroutineScope.showRoutineAmountInvalid(
                    amountState = state.amountPerRecordState,
                    snackbarHostState = state.snackbarHostState,
                    message = amountPerRecordInvalidMessage,
                )
            }
        }
    }
}
