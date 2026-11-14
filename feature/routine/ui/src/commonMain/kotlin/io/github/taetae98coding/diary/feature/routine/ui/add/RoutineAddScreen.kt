package io.github.taetae98coding.diary.feature.routine.ui.add

import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.input.DiaryTitleInputFocusEffect
import io.github.taetae98coding.diary.feature.core.tag.EntityAddTagViewModel
import io.github.taetae98coding.diary.feature.core.tag.EntityTagAddedResultEffect
import io.github.taetae98coding.diary.feature.core.tag.handleEntityTagPickerEvent
import io.github.taetae98coding.diary.feature.routine.ui.form.handleRoutineFormEvent
import io.github.taetae98coding.diary.feature.routine.ui.form.rememberRoutineAddFormState
import kotlin.uuid.Uuid

@Composable
internal fun RoutineAddScreen(
    navigateUp: () -> Unit,
    navigateToTagAdd: () -> Unit,
    navigateToTagDetail: (Uuid) -> Unit,
    tagAddRequestKey: Uuid,
    componentVisibleProvider: () -> RoutineAddScaffoldComponentVisible,
    addViewModel: RoutineAddViewModel,
    tagViewModel: EntityAddTagViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by addViewModel.uiState.collectAsStateWithLifecycle()
    val tagUiState by tagViewModel.uiState.collectAsStateWithLifecycle()
    val tagPagingItems = tagViewModel.tagPagingData.collectAsLazyPagingItems()
    val selectableTagPagingItems = tagViewModel.selectableTagPagingData.collectAsLazyPagingItems()
    val state = rememberRoutineAddFormState()
    val scrollState = rememberScrollState()

    DiaryTitleInputFocusEffect(state = state.titleState)
    EntityTagAddedResultEffect(
        requestKey = tagAddRequestKey,
        onTagAdded = tagViewModel::add,
    )
    RoutineAddScreenEffect(
        effect = addViewModel.effect,
        state = state,
        scrollState = scrollState,
    )

    RoutineAddScaffold(
        onEvent = { event ->
            when (event) {
                is RoutineAddScaffoldEvent.ClickNavigateUp -> {
                    navigateUp()
                }

                is RoutineAddScaffoldEvent.ClickAdd -> {
                    addViewModel.add(
                        input = state.input,
                        tagIdSet = tagViewModel.selectionUiState.value.tagIdSet,
                    )
                }
            }
        },
        onFormEvent = { event ->
            handleRoutineFormEvent(
                event = event,
                state = state,
                selectableTagPagingItems = selectableTagPagingItems,
                navigateToTagAdd = navigateToTagAdd,
                navigateToTagDetail = navigateToTagDetail,
            )
        },
        onTagPickerEvent = { event -> handleEntityTagPickerEvent(event = event, tagViewModel = tagViewModel, navigateToTagAdd = navigateToTagAdd) },
        modifier = modifier,
        state = state,
        scrollState = scrollState,
        tagPagingItems = tagPagingItems,
        uiStateProvider = { uiState },
        tagUiStateProvider = { tagUiState },
        componentVisibleProvider = componentVisibleProvider,
    )
}
