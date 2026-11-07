package io.github.taetae98coding.diary.feature.playlist.ui.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.input.DiaryTitleInputFocusEffect
import io.github.taetae98coding.diary.feature.core.tag.EntityAddTagViewModel
import io.github.taetae98coding.diary.feature.core.tag.EntityTagAddedResultEffect
import io.github.taetae98coding.diary.feature.core.tag.handleEntityTagPickerEvent
import io.github.taetae98coding.diary.feature.playlist.ui.form.handleMusicFormEvent
import io.github.taetae98coding.diary.feature.playlist.ui.form.rememberMusicAddFormState
import kotlin.uuid.Uuid

@Composable
internal fun MusicAddScreen(
    navigateUp: () -> Unit,
    navigateToTagAdd: () -> Unit,
    navigateToTagDetail: (Uuid) -> Unit,
    tagAddRequestKey: Uuid,
    componentVisibleProvider: () -> MusicAddScaffoldComponentVisible,
    addViewModel: MusicAddViewModel,
    tagViewModel: EntityAddTagViewModel,
    modifier: Modifier = Modifier,
) {
    val state = rememberMusicAddFormState()
    val uiState by addViewModel.uiState.collectAsStateWithLifecycle()
    val tagUiState by tagViewModel.uiState.collectAsStateWithLifecycle()
    val tagPagingItems = tagViewModel.tagPagingData.collectAsLazyPagingItems()
    val selectableTagPagingItems = tagViewModel.selectableTagPagingData.collectAsLazyPagingItems()

    DiaryTitleInputFocusEffect(state = state.titleState)
    EntityTagAddedResultEffect(
        requestKey = tagAddRequestKey,
        onTagAdded = tagViewModel::add,
    )
    MusicAddScreenEffect(
        effect = addViewModel.effect,
        state = state,
    )

    MusicAddScaffold(
        onEvent = { event ->
            when (event) {
                is MusicAddScaffoldEvent.ClickNavigateUp -> navigateUp()

                is MusicAddScaffoldEvent.ClickAdd ->
                    addViewModel.add(
                        detail = state.detail,
                        tagIdSet = tagViewModel.selectionUiState.value.tagIdSet,
                    )

                is MusicAddScaffoldEvent.ClickFetchLink -> addViewModel.fetchLink(link = state.link)
            }
        },
        onFormEvent = { event ->
            handleMusicFormEvent(
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
