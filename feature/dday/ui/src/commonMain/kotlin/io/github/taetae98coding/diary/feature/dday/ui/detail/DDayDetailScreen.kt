package io.github.taetae98coding.diary.feature.dday.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.feature.core.tag.EntityDetailTagViewModel
import io.github.taetae98coding.diary.feature.core.tag.EntityTagAddedResultEffect
import io.github.taetae98coding.diary.feature.core.tag.handleEntityTagPickerEvent
import io.github.taetae98coding.diary.feature.dday.ui.form.handleDDayFormEvent
import io.github.taetae98coding.diary.feature.dday.ui.form.rememberDDayDetailFormState
import kotlin.uuid.Uuid

@Composable
internal fun DDayDetailScreen(
    navigateUp: () -> Unit,
    navigateToTagAdd: () -> Unit,
    navigateToTagDetail: (Uuid) -> Unit,
    tagAddRequestKey: Uuid,
    componentVisibleProvider: () -> DDayDetailScaffoldComponentVisible,
    ddayViewModel: DDayDetailViewModel,
    tagViewModel: EntityDetailTagViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by ddayViewModel.uiState.collectAsStateWithLifecycle()
    val tagUiState by tagViewModel.uiState.collectAsStateWithLifecycle()
    val tagPagingItems = tagViewModel.tagPagingData.collectAsLazyPagingItems()
    val selectableTagPagingItems = tagViewModel.selectableTagPagingData.collectAsLazyPagingItems()
    val content = uiState as? DDayDetailUiState.Content

    EntityTagAddedResultEffect(requestKey = tagAddRequestKey, onTagAdded = tagViewModel::add)

    key(content?.id) {
        val state = rememberDDayDetailFormState(initialDetail = content?.detail)

        DDayDetailScreenEffect(
            navigateUp = navigateUp,
            effect = ddayViewModel.effect,
            state = state,
        )

        DDayDetailScaffold(
            onEvent = { event ->
                when (event) {
                    is DDayDetailScaffoldEvent.ClickNavigateUp -> navigateUp()
                    is DDayDetailScaffoldEvent.ClickDelete -> ddayViewModel.delete()
                    is DDayDetailScaffoldEvent.ClickUpdate -> ddayViewModel.update(input = state.input)
                }
            },
            onFormEvent = { event ->
                handleDDayFormEvent(
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
}
