package io.github.taetae98coding.diary.feature.routine.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.feature.core.tag.EntityDetailTagViewModel
import io.github.taetae98coding.diary.feature.core.tag.EntityTagAddedResultEffect
import io.github.taetae98coding.diary.feature.core.tag.handleEntityTagPickerEvent
import io.github.taetae98coding.diary.feature.routine.ui.detail.record.RoutineDetailRecordViewModel
import io.github.taetae98coding.diary.feature.routine.ui.form.handleRoutineFormEvent
import kotlin.uuid.Uuid

@Composable
internal fun RoutineDetailScreen(
    navigateUp: () -> Unit,
    navigateToTagAdd: () -> Unit,
    navigateToTagDetail: (Uuid) -> Unit,
    tagAddRequestKey: Uuid,
    componentVisibleProvider: () -> RoutineDetailScaffoldComponentVisible,
    routineViewModel: RoutineDetailViewModel,
    recordViewModel: RoutineDetailRecordViewModel,
    tagViewModel: EntityDetailTagViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by routineViewModel.uiState.collectAsStateWithLifecycle()
    val recordUiState by recordViewModel.uiState.collectAsStateWithLifecycle()
    val tagUiState by tagViewModel.uiState.collectAsStateWithLifecycle()
    val recordPagingItems = recordViewModel.recordPagingData.collectAsLazyPagingItems()
    val tagPagingItems = tagViewModel.tagPagingData.collectAsLazyPagingItems()
    val selectableTagPagingItems = tagViewModel.selectableTagPagingData.collectAsLazyPagingItems()
    val content = uiState as? RoutineDetailUiState.Content

    EntityTagAddedResultEffect(requestKey = tagAddRequestKey, onTagAdded = tagViewModel::add)

    key(content?.id) {
        val state = rememberRoutineDetailScaffoldState(initialDetail = content?.detail)

        RoutineDetailScreenEffect(
            navigateUp = navigateUp,
            effect = routineViewModel.effect,
            state = state,
        )
        RoutineDetailRecordAddEffect(
            effect = recordViewModel.effect,
            state = state,
            uiStateProvider = { uiState },
        )
        RoutineDetailRecordDateRangeEffect(
            state = state,
            uiStateProvider = { uiState },
        )

        RoutineDetailScaffold(
            onEvent = { event ->
                handleRoutineDetailScaffoldEvent(
                    event = event,
                    state = state,
                    routineViewModel = routineViewModel,
                    recordViewModel = recordViewModel,
                    navigateUp = navigateUp,
                )
            },
            onFormEvent = { event ->
                handleRoutineFormEvent(
                    event = event,
                    state = state.formState,
                    selectableTagPagingItems = selectableTagPagingItems,
                    navigateToTagAdd = navigateToTagAdd,
                    navigateToTagDetail = navigateToTagDetail,
                )
            },
            onTagPickerEvent = { event -> handleEntityTagPickerEvent(event = event, tagViewModel = tagViewModel, navigateToTagAdd = navigateToTagAdd) },
            modifier = modifier,
            state = state,
            tagPagingItems = tagPagingItems,
            recordPagingItems = recordPagingItems,
            uiStateProvider = { uiState },
            recordUiStateProvider = { recordUiState },
            tagUiStateProvider = { tagUiState },
            componentVisibleProvider = componentVisibleProvider,
        )
    }
}

private fun handleRoutineDetailScaffoldEvent(
    event: RoutineDetailScaffoldEvent,
    state: RoutineDetailScaffoldState,
    routineViewModel: RoutineDetailViewModel,
    recordViewModel: RoutineDetailRecordViewModel,
    navigateUp: () -> Unit,
) {
    when (event) {
        is RoutineDetailScaffoldEvent.ClickNavigateUp -> navigateUp()

        is RoutineDetailScaffoldEvent.ClickFinish -> routineViewModel.finish()

        is RoutineDetailScaffoldEvent.ClickRestart -> routineViewModel.restart()

        is RoutineDetailScaffoldEvent.ClickDelete -> routineViewModel.delete()

        is RoutineDetailScaffoldEvent.ClickUpdate -> routineViewModel.update(input = state.formState.input)

        is RoutineDetailScaffoldEvent.ClickRecordAdd -> {
            val amount = state.recordInputState.amountState.amount
            if (state.recordInputState.isAddable && amount != null) {
                recordViewModel.add(date = state.recordInputState.date, amount = amount)
            }
        }

        is RoutineDetailScaffoldEvent.ClickRecordRemove -> recordViewModel.delete(recordId = event.id)
    }
}
