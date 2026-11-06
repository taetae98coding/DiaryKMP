package io.github.taetae98coding.diary.feature.checklist.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.core.model.checklist.ChecklistDetail
import io.github.taetae98coding.diary.core.model.checklist.ChecklistItemSort
import io.github.taetae98coding.diary.feature.checklist.ui.tag.handleChecklistTagInputEvent
import io.github.taetae98coding.diary.feature.core.tag.EntityDetailTagViewModel
import io.github.taetae98coding.diary.feature.core.tag.EntityTagAddedResultEffect
import io.github.taetae98coding.diary.feature.core.tag.handleEntityTagPickerEvent
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

@Composable
internal fun ChecklistDetailScreen(
    navigateUp: () -> Unit,
    navigateToTagAdd: () -> Unit,
    navigateToTagDetail: (Uuid) -> Unit,
    tagAddRequestKey: Uuid,
    componentVisibleProvider: () -> ChecklistDetailScaffoldComponentVisible,
    checklistViewModel: ChecklistDetailViewModel,
    itemViewModel: ChecklistDetailItemViewModel,
    tagViewModel: EntityDetailTagViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by checklistViewModel.uiState.collectAsStateWithLifecycle()
    val itemUiState by itemViewModel.uiState.collectAsStateWithLifecycle()
    val tagUiState by tagViewModel.uiState.collectAsStateWithLifecycle()
    val tagPagingItems = tagViewModel.tagPagingData.collectAsLazyPagingItems()
    val selectableTagPagingItems = tagViewModel.selectableTagPagingData.collectAsLazyPagingItems()
    val content = uiState as? ChecklistDetailUiState.Content
    val coroutineScope = rememberCoroutineScope()

    EntityTagAddedResultEffect(requestKey = tagAddRequestKey, onTagAdded = tagViewModel::add)

    key(content?.id) {
        val state = rememberChecklistDetailFormState(initialDetail = content?.detail ?: ChecklistDetail.EMPTY)

        ChecklistDetailScreenEffect(
            navigateUp = navigateUp,
            effect = checklistViewModel.effect,
            state = state,
        )
        ChecklistDetailItemAddEffect(
            effect = itemViewModel.effect,
            state = state,
        )

        ChecklistDetailScaffold(
            onEvent = { event ->
                handleChecklistDetailScaffoldEvent(
                    event = event,
                    state = state,
                    currentSort = itemUiState.sort,
                    checklistViewModel = checklistViewModel,
                    itemViewModel = itemViewModel,
                    scrollToItemList = { coroutineScope.launch { state.scrollToItemList() } },
                    navigateUp = navigateUp,
                )
            },
            onTagInputEvent = { event ->
                handleChecklistTagInputEvent(
                    event = event,
                    tagPickerDialogState = state.tagPickerDialogState,
                    selectableTagPagingItems = selectableTagPagingItems,
                    navigateToTagAdd = navigateToTagAdd,
                    navigateToTagDetail = navigateToTagDetail,
                )
            },
            onTagPickerEvent = { event -> handleEntityTagPickerEvent(event = event, tagViewModel = tagViewModel, navigateToTagAdd = navigateToTagAdd) },
            modifier = modifier,
            state = state,
            tagPagingItems = tagPagingItems,
            uiStateProvider = { uiState },
            itemUiStateProvider = { itemUiState },
            tagUiStateProvider = { tagUiState },
            componentVisibleProvider = componentVisibleProvider,
        )
    }
}

private fun handleChecklistDetailScaffoldEvent(
    event: ChecklistDetailScaffoldEvent,
    state: ChecklistDetailFormState,
    currentSort: ChecklistItemSort,
    checklistViewModel: ChecklistDetailViewModel,
    itemViewModel: ChecklistDetailItemViewModel,
    scrollToItemList: () -> Unit,
    navigateUp: () -> Unit,
) {
    when (event) {
        is ChecklistDetailScaffoldEvent.ClickNavigateUp -> navigateUp()

        is ChecklistDetailScaffoldEvent.ClickReset -> state.resetDialogState.show()

        is ChecklistDetailScaffoldEvent.ConfirmReset -> itemViewModel.reset()

        is ChecklistDetailScaffoldEvent.ClickDelete -> checklistViewModel.delete()

        is ChecklistDetailScaffoldEvent.ClickUpdate -> checklistViewModel.update(detail = state.detail)

        is ChecklistDetailScaffoldEvent.ClickSort -> state.sortSheetState.show()

        is ChecklistDetailScaffoldEvent.SelectSort -> {
            if (currentSort != event.sort) {
                itemViewModel.selectSort(sort = event.sort)
                scrollToItemList()
            }
        }

        is ChecklistDetailScaffoldEvent.ClickItem -> itemViewModel.toggle(id = event.id, isChecked = event.isChecked)

        is ChecklistDetailScaffoldEvent.ClickItemRemove -> itemViewModel.delete(itemId = event.id)

        is ChecklistDetailScaffoldEvent.ClickItemAdd -> {
            if (state.itemInputState.isAddable) {
                itemViewModel.add(content = state.itemInputState.content)
            }
        }
    }
}
