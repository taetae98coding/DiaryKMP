package io.github.taetae98coding.diary.feature.place.ui.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.input.DiaryTitleInputFocusEffect
import io.github.taetae98coding.diary.compose.place.toDiaryMapCoordinate
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagPickerEvent
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.feature.place.ui.form.PlaceFormState
import io.github.taetae98coding.diary.feature.place.ui.form.handlePlaceFormEvent
import io.github.taetae98coding.diary.feature.place.ui.form.rememberPlaceAddFormState
import io.github.taetae98coding.diary.feature.place.ui.search.PlaceSearchEvent
import io.github.taetae98coding.diary.feature.place.ui.search.PlaceSearchViewModel
import io.github.taetae98coding.diary.feature.place.ui.tag.PlaceTagAddedResultEffect
import kotlin.uuid.Uuid

@Composable
internal fun PlaceAddScreen(
    navigateUp: () -> Unit,
    navigateToTagAdd: () -> Unit,
    navigateToTagDetail: (Uuid) -> Unit,
    tagAddRequestKey: Uuid,
    addedResultRequestKey: Uuid?,
    initialCoordinate: Coordinate?,
    addViewModel: PlaceAddViewModel,
    searchViewModel: PlaceSearchViewModel,
    tagViewModel: PlaceAddTagViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by addViewModel.uiState.collectAsStateWithLifecycle()
    val searchUiState by searchViewModel.uiState.collectAsStateWithLifecycle()
    val tagUiState by tagViewModel.uiState.collectAsStateWithLifecycle()
    val tagPagingItems = tagViewModel.tagPagingData.collectAsLazyPagingItems()
    val selectableTagPagingItems = tagViewModel.selectableTagPagingData.collectAsLazyPagingItems()
    val scaffoldState =
        rememberPlaceAddFormState(
            defaultProvider = uiState.defaultProvider,
            initialCoordinate = initialCoordinate?.toDiaryMapCoordinate(),
        )

    DiaryTitleInputFocusEffect(state = scaffoldState.titleState)
    PlaceTagAddedResultEffect(
        requestKey = tagAddRequestKey,
        onTagAdded = tagViewModel::add,
    )
    PlaceAddScreenEffect(
        addedResultRequestKey = addedResultRequestKey,
        effect = addViewModel.effect,
        scaffoldState = scaffoldState,
    )

    PlaceAddScaffold(
        state = scaffoldState,
        tagPagingItems = tagPagingItems,
        uiStateProvider = { uiState },
        searchUiStateProvider = { searchUiState },
        tagUiStateProvider = { tagUiState },
        onEvent = { event ->
            handlePlaceAddScaffoldEvent(
                event = event,
                addViewModel = addViewModel,
                tagViewModel = tagViewModel,
                scaffoldState = scaffoldState,
                navigateUp = navigateUp,
            )
        },
        onFormEvent = { event ->
            handlePlaceFormEvent(
                event = event,
                state = scaffoldState,
                selectableTagPagingItems = selectableTagPagingItems,
                navigateToTagAdd = navigateToTagAdd,
                navigateToTagDetail = navigateToTagDetail,
            )
        },
        onSearchEvent = { event ->
            searchViewModel.handlePlaceAddSearchEvent(event = event, scaffoldState = scaffoldState)
        },
        onTagPickerEvent = { event ->
            handlePlaceAddTagPickerEvent(
                event = event,
                tagViewModel = tagViewModel,
                navigateToTagAdd = navigateToTagAdd,
            )
        },
        modifier = modifier,
    )
}

private fun handlePlaceAddScaffoldEvent(
    event: PlaceAddScaffoldEvent,
    addViewModel: PlaceAddViewModel,
    tagViewModel: PlaceAddTagViewModel,
    scaffoldState: PlaceFormState,
    navigateUp: () -> Unit,
) {
    when (event) {
        is PlaceAddScaffoldEvent.ClickNavigateUp -> navigateUp()

        is PlaceAddScaffoldEvent.ClickAdd ->
            addViewModel.add(
                detail = scaffoldState.detail,
                tagIdSet = tagViewModel.selectionUiState.value.tagIdSet,
            )

        is PlaceAddScaffoldEvent.ClickSearch -> scaffoldState.searchDialogState.show()
    }
}

private fun PlaceSearchViewModel.handlePlaceAddSearchEvent(
    event: PlaceSearchEvent,
    scaffoldState: PlaceFormState,
) {
    when (event) {
        is PlaceSearchEvent.Search -> search(request = event.request)
        is PlaceSearchEvent.ClearSearch -> clear()
        is PlaceSearchEvent.Select -> scaffoldState.applySearchedPlace(event.place)
    }
}

private fun handlePlaceAddTagPickerEvent(
    event: EntityTagPickerEvent,
    tagViewModel: PlaceAddTagViewModel,
    navigateToTagAdd: () -> Unit,
) {
    when (event) {
        is EntityTagPickerEvent.ClickAdd -> navigateToTagAdd()
        is EntityTagPickerEvent.Add -> tagViewModel.add(id = event.id)
        is EntityTagPickerEvent.Remove -> tagViewModel.remove(id = event.id)
        is EntityTagPickerEvent.ChangeQuery -> tagViewModel.updateQuery(query = event.query)
    }
}
