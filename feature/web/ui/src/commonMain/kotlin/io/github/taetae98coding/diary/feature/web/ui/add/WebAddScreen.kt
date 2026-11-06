package io.github.taetae98coding.diary.feature.web.ui.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.input.DiaryTitleInputFocusEffect
import io.github.taetae98coding.diary.feature.core.tag.EntityAddTagViewModel
import io.github.taetae98coding.diary.feature.core.tag.EntityTagAddedResultEffect
import io.github.taetae98coding.diary.feature.core.tag.handleEntityTagPickerEvent
import io.github.taetae98coding.diary.feature.web.ui.form.handleWebFormEvent
import io.github.taetae98coding.diary.feature.web.ui.form.rememberWebAddFormState
import kotlin.uuid.Uuid

@Composable
internal fun WebAddScreen(
    navigateUp: () -> Unit,
    navigateToTagAdd: () -> Unit,
    navigateToTagDetail: (Uuid) -> Unit,
    tagAddRequestKey: Uuid,
    addedResultRequestKey: Uuid?,
    componentVisibleProvider: () -> WebAddScaffoldComponentVisible,
    addViewModel: WebAddViewModel,
    tagViewModel: EntityAddTagViewModel,
    modifier: Modifier = Modifier,
) {
    val state = rememberWebAddFormState()
    val uiState by addViewModel.uiState.collectAsStateWithLifecycle()
    val tagUiState by tagViewModel.uiState.collectAsStateWithLifecycle()
    val tagPagingItems = tagViewModel.tagPagingData.collectAsLazyPagingItems()
    val selectableTagPagingItems = tagViewModel.selectableTagPagingData.collectAsLazyPagingItems()

    DiaryTitleInputFocusEffect(state = state.titleState)
    EntityTagAddedResultEffect(
        requestKey = tagAddRequestKey,
        onTagAdded = tagViewModel::add,
    )
    WebAddScreenEffect(
        addedResultRequestKey = addedResultRequestKey,
        effect = addViewModel.effect,
        state = state,
    )

    WebAddScaffold(
        onEvent = { event ->
            when (event) {
                is WebAddScaffoldEvent.ClickNavigateUp -> navigateUp()

                is WebAddScaffoldEvent.ClickAdd ->
                    addViewModel.add(
                        detail = state.detail,
                        tagIdSet = tagViewModel.selectionUiState.value.tagIdSet,
                    )
            }
        },
        onFormEvent = { event ->
            handleWebFormEvent(
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
        tagPagingItems = tagPagingItems,
        uiStateProvider = { uiState },
        tagUiStateProvider = { tagUiState },
        componentVisibleProvider = componentVisibleProvider,
    )
}
