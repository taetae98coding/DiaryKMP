package io.github.taetae98coding.diary.feature.checklist.ui.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.feature.checklist.ui.Res
import io.github.taetae98coding.diary.feature.checklist.ui.checklist_add_succeeded_message
import io.github.taetae98coding.diary.feature.checklist.ui.checklist_add_title_blank_message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ChecklistAddScreenEffect(
    effect: Flow<ChecklistAddEffect> = emptyFlow(),
    state: ChecklistAddFormState = rememberChecklistAddFormState(),
) {
    val coroutineScope = rememberCoroutineScope()
    val addSucceededMessage = stringResource(Res.string.checklist_add_succeeded_message)
    val titleBlankMessage = stringResource(Res.string.checklist_add_title_blank_message)

    CollectEffect(effect) { value ->
        when (value) {
            is ChecklistAddEffect.AddSucceeded -> {
                state.clear()
                state.scrollState.scrollTo(0)
                state.titleState.requestFocus()
                coroutineScope.launch { state.snackbarHostState.showImmediate(message = addSucceededMessage) }
            }

            is ChecklistAddEffect.TitleBlank -> {
                state.scrollState.scrollTo(0)
                state.titleState.requestFocus()
                coroutineScope.launch { state.snackbarHostState.showImmediate(message = titleBlankMessage) }
            }
        }
    }
}
