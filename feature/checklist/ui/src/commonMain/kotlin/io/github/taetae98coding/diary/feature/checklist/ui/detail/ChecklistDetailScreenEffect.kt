package io.github.taetae98coding.diary.feature.checklist.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.feature.checklist.ui.Res
import io.github.taetae98coding.diary.feature.checklist.ui.checklist_detail_update_succeeded_message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ChecklistDetailScreenEffect(
    navigateUp: () -> Unit,
    effect: Flow<ChecklistDetailEffect> = emptyFlow(),
    state: ChecklistDetailFormState = rememberChecklistDetailFormState(),
) {
    val coroutineScope = rememberCoroutineScope()
    val updateSucceededMessage = stringResource(Res.string.checklist_detail_update_succeeded_message)

    CollectEffect(effect) { value ->
        when (value) {
            is ChecklistDetailEffect.UpdateSucceeded -> {
                coroutineScope.launch { state.snackbarHostState.showImmediate(message = updateSucceededMessage) }
            }

            is ChecklistDetailEffect.DeleteSucceeded -> {
                navigateUp()
            }
        }
    }
}

@Composable
internal fun ChecklistDetailItemAddEffect(
    effect: Flow<ChecklistDetailItemEffect> = emptyFlow(),
    state: ChecklistDetailFormState = rememberChecklistDetailFormState(),
) {
    CollectEffect(effect) { value ->
        when (value) {
            is ChecklistDetailItemEffect.AddSucceeded -> {
                state.itemInputState.clear()
                state.bringItemInputIntoView()
            }
        }
    }
}
