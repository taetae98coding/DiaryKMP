package io.github.taetae98coding.diary.feature.routine.ui.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.input.DiaryColorInput
import io.github.taetae98coding.diary.compose.core.input.DiaryDescriptionInput
import io.github.taetae98coding.diary.compose.core.input.DiaryTitleInput
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagInput
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagInputUiState
import io.github.taetae98coding.diary.feature.routine.ui.Res
import io.github.taetae98coding.diary.feature.routine.ui.routine_amount_per_record_label
import io.github.taetae98coding.diary.feature.routine.ui.routine_goal_amount_label
import io.github.taetae98coding.diary.feature.routine.ui.routine_goal_amount_supporting_text
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun RoutineForm(
    onEvent: (RoutineFormEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: RoutineFormState = rememberRoutineAddFormState(),
    tagUiStateProvider: () -> EntityTagInputUiState = { EntityTagInputUiState() },
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.componentSpacing),
    ) {
        DiaryTitleInput(
            state = state.titleState,
            modifier = Modifier.fillMaxWidth(),
        )
        DiaryDescriptionInput(
            state = state.descriptionState,
            modifier = Modifier.fillMaxWidth(),
        )
        RoutineStartDateCard(
            modifier = Modifier.fillMaxWidth(),
            state = state,
        )
        RoutineCycleCard(
            modifier = Modifier.fillMaxWidth(),
            state = state,
        )
        RoutineUnitTextField(
            modifier = Modifier.fillMaxWidth(),
            state = state.unitState,
        )
        RoutineAmountTextField(
            label = stringResource(Res.string.routine_goal_amount_label),
            modifier = Modifier.fillMaxWidth(),
            state = state.goalAmountState,
            unitProvider = { state.unitState.unit },
            supportingText = stringResource(Res.string.routine_goal_amount_supporting_text),
        )
        RoutineAmountTextField(
            label = stringResource(Res.string.routine_amount_per_record_label),
            modifier = Modifier.fillMaxWidth(),
            state = state.amountPerRecordState,
            unitProvider = { state.unitState.unit },
        )
        DiaryColorInput(
            modifier = Modifier.fillMaxWidth(),
            state = state.colorState,
        )
        EntityTagInput(
            onTagClick = { id -> onEvent(RoutineFormEvent.ClickTag(id = id)) },
            onAddClick = { onEvent(RoutineFormEvent.ClickTagAdd) },
            modifier = Modifier.fillMaxWidth(),
            uiStateProvider = tagUiStateProvider,
        )
    }
}

@ComponentPreview
@Composable
private fun RoutineFormPreview() {
    DiaryTheme {
        Surface {
            RoutineForm(onEvent = {})
        }
    }
}
