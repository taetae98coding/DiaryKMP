package io.github.taetae98coding.diary.feature.checklist.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.taetae98coding.diary.core.model.checklist.ChecklistDetail
import kotlinx.coroutines.launch

@Composable
internal fun ChecklistDetailScreen(
    navigateUp: () -> Unit,
    checklistViewModel: ChecklistDetailViewModel,
    itemViewModel: ChecklistDetailItemViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by checklistViewModel.uiState.collectAsStateWithLifecycle()
    val itemUiState by itemViewModel.uiState.collectAsStateWithLifecycle()
    val content = uiState as? ChecklistDetailUiState.Content
    val coroutineScope = rememberCoroutineScope()

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
                when (event) {
                    is ChecklistDetailScaffoldEvent.ClickNavigateUp -> navigateUp()

                    is ChecklistDetailScaffoldEvent.ClickReset -> state.resetDialogState.show()

                    is ChecklistDetailScaffoldEvent.ConfirmReset -> itemViewModel.reset()

                    is ChecklistDetailScaffoldEvent.ClickDelete -> checklistViewModel.delete()

                    is ChecklistDetailScaffoldEvent.ClickUpdate -> checklistViewModel.update(detail = state.detail)

                    is ChecklistDetailScaffoldEvent.ClickSort -> state.sortSheetState.show()

                    is ChecklistDetailScaffoldEvent.SelectSort -> {
                        if (itemUiState.sort != event.sort) {
                            itemViewModel.selectSort(sort = event.sort)
                            coroutineScope.launch { state.scrollToItemList() }
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
            },
            modifier = modifier,
            state = state,
            uiStateProvider = { uiState },
            itemUiStateProvider = { itemUiState },
        )
    }
}
