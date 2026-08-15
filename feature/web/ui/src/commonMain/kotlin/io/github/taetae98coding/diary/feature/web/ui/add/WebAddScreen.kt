package io.github.taetae98coding.diary.feature.web.ui.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.input.DiaryTitleInputFocusEffect
import io.github.taetae98coding.diary.compose.tag.EntityTagPickerEvent
import io.github.taetae98coding.diary.feature.web.ui.form.handleWebFormEvent
import io.github.taetae98coding.diary.feature.web.ui.form.rememberWebAddFormState
import io.github.taetae98coding.diary.feature.web.ui.tag.WebTagAddedResultEffect
import kotlin.uuid.Uuid

@Composable
internal fun WebAddScreen(
    navigateUp: () -> Unit,
    navigateToTagAdd: () -> Unit,
    navigateToTagDetail: (Uuid) -> Unit,
    tagAddRequestKey: Uuid,
    componentVisibleProvider: () -> WebAddScaffoldComponentVisible,
    addViewModel: WebAddViewModel,
    tagViewModel: WebAddTagViewModel,
    modifier: Modifier = Modifier,
) {
    val state = rememberWebAddFormState()
    val uiState by addViewModel.uiState.collectAsStateWithLifecycle()
    val tagUiState by tagViewModel.uiState.collectAsStateWithLifecycle()
    val tagPagingItems = tagViewModel.tagPagingData.collectAsLazyPagingItems()

    DiaryTitleInputFocusEffect(state = state.titleState)
    WebTagAddedResultEffect(
        requestKey = tagAddRequestKey,
        onTagAdded = tagViewModel::add,
    )
    WebAddScreenEffect(
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
                        tagIdSet = tagViewModel.tagIdSet.value,
                    )
            }
        },
        onFormEvent = { event ->
            handleWebFormEvent(
                event = event,
                state = state,
                tagPagingItems = tagPagingItems,
                navigateToTagAdd = navigateToTagAdd,
                navigateToTagDetail = navigateToTagDetail,
            )
        },
        onTagPickerEvent = { event ->
            when (event) {
                is EntityTagPickerEvent.ClickAdd -> navigateToTagAdd()
                is EntityTagPickerEvent.Add -> tagViewModel.add(id = event.id)
                is EntityTagPickerEvent.Remove -> tagViewModel.remove(id = event.id)
                is EntityTagPickerEvent.ChangeQuery -> tagViewModel.updateQuery(query = event.query)
            }
        },
        modifier = modifier,
        state = state,
        tagPagingItems = tagPagingItems,
        uiStateProvider = { uiState },
        tagUiStateProvider = { tagUiState },
        componentVisibleProvider = componentVisibleProvider,
    )
}
