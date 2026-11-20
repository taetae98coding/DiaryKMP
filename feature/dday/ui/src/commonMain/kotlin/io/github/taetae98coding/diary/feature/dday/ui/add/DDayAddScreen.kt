package io.github.taetae98coding.diary.feature.dday.ui.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.input.DiaryTitleInputFocusEffect
import io.github.taetae98coding.diary.feature.core.tag.TagAddedResultEffect
import io.github.taetae98coding.diary.feature.core.tag.handleEntityTagInputEvent
import io.github.taetae98coding.diary.feature.core.tag.handlePrimaryTagPickerEvent
import io.github.taetae98coding.diary.feature.dday.ui.form.rememberDDayAddFormState
import kotlin.uuid.Uuid

@Composable
internal fun DDayAddScreen(
    navigateUp: () -> Unit,
    navigateToTagAdd: () -> Unit,
    navigateToTagDetail: (Uuid) -> Unit,
    tagAddRequestKey: Uuid,
    componentVisibleProvider: () -> DDayAddScaffoldComponentVisible,
    addViewModel: DDayAddViewModel,
    tagViewModel: DDayAddTagViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by addViewModel.uiState.collectAsStateWithLifecycle()
    val tagUiState by tagViewModel.uiState.collectAsStateWithLifecycle()
    val tagPagingItems = tagViewModel.tagPagingData.collectAsLazyPagingItems()
    val selectableTagPagingItems = tagViewModel.selectableTagPagingData.collectAsLazyPagingItems()
    val state = rememberDDayAddFormState()

    DiaryTitleInputFocusEffect(state = state.titleState)
    TagAddedResultEffect(
        requestKey = tagAddRequestKey,
        onTagAdded = tagViewModel::selectTag,
    )
    DDayAddScreenEffect(
        effect = addViewModel.effect,
        state = state,
    )

    DDayAddScaffold(
        onEvent = { event ->
            when (event) {
                is DDayAddScaffoldEvent.ClickNavigateUp -> {
                    navigateUp()
                }

                is DDayAddScaffoldEvent.ClickAdd -> {
                    addViewModel.add(
                        input = state.input,
                        tagSelection = tagViewModel.selectionUiState.value,
                    )
                }
            }
        },
        onFormEvent = { event ->
            handleEntityTagInputEvent(
                event = event,
                tagPickerDialogState = state.tagPickerDialogState,
                selectableTagPagingItems = selectableTagPagingItems,
                navigateToTagAdd = navigateToTagAdd,
                navigateToTagDetail = navigateToTagDetail,
            )
        },
        onTagPickerEvent = { event -> handlePrimaryTagPickerEvent(event = event, tagViewModel = tagViewModel, navigateToTagAdd = navigateToTagAdd) },
        modifier = modifier,
        state = state,
        tagPagingItems = tagPagingItems,
        uiStateProvider = { uiState },
        tagUiStateProvider = { tagUiState },
        componentVisibleProvider = componentVisibleProvider,
    )
}
