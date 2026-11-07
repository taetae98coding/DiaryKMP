package io.github.taetae98coding.diary.feature.checklist.ui.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.input.DiaryTitleInputFocusEffect
import io.github.taetae98coding.diary.feature.checklist.ui.tag.handleChecklistTagInputEvent
import io.github.taetae98coding.diary.feature.core.tag.EntityAddTagViewModel
import io.github.taetae98coding.diary.feature.core.tag.EntityTagAddedResultEffect
import io.github.taetae98coding.diary.feature.core.tag.handleEntityTagPickerEvent
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

@Composable
internal fun ChecklistAddScreen(
    navigateUp: () -> Unit,
    navigateToTagAdd: () -> Unit,
    navigateToTagDetail: (Uuid) -> Unit,
    tagAddRequestKey: Uuid,
    componentVisibleProvider: () -> ChecklistAddScaffoldComponentVisible,
    addViewModel: ChecklistAddViewModel,
    tagViewModel: EntityAddTagViewModel,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val uiState by addViewModel.uiState.collectAsStateWithLifecycle()
    val tagUiState by tagViewModel.uiState.collectAsStateWithLifecycle()
    val tagPagingItems = tagViewModel.tagPagingData.collectAsLazyPagingItems()
    val selectableTagPagingItems = tagViewModel.selectableTagPagingData.collectAsLazyPagingItems()
    val state = rememberChecklistAddFormState()

    DiaryTitleInputFocusEffect(state = state.titleState)
    EntityTagAddedResultEffect(
        requestKey = tagAddRequestKey,
        onTagAdded = tagViewModel::add,
    )
    ChecklistAddScreenEffect(
        effect = addViewModel.effect,
        state = state,
    )

    ChecklistAddScaffold(
        onEvent = { event ->
            when (event) {
                is ChecklistAddScaffoldEvent.ClickNavigateUp -> {
                    navigateUp()
                }

                is ChecklistAddScaffoldEvent.ClickAdd -> {
                    addViewModel.add(
                        detail = state.detail,
                        itemContentList = state.itemContentList,
                        tagIdSet = tagViewModel.selectionUiState.value.tagIdSet,
                    )
                }

                is ChecklistAddScaffoldEvent.ClickItemAdd -> {
                    if (state.addItem()) {
                        coroutineScope.launch {
                            // 새 행이 배치된 뒤에야 밀려난 항목 입력의 자리를 알 수 있다.
                            withFrameNanos { }
                            state.itemInputBringIntoViewRequester.bringIntoView()
                        }
                    }
                }

                is ChecklistAddScaffoldEvent.ClickItemRemove -> {
                    state.removeItem(key = event.key)
                }
            }
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
        tagUiStateProvider = { tagUiState },
        componentVisibleProvider = componentVisibleProvider,
    )
}
