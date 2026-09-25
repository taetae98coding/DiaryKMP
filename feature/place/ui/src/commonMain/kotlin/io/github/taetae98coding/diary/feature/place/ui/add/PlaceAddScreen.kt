package io.github.taetae98coding.diary.feature.place.ui.add

import androidx.compose.foundation.text.input.clearText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.result.LocalResultEventBus
import androidx.navigation3.runtime.result.ResultEventBus
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import io.github.taetae98coding.diary.compose.core.input.DiaryTitleInputFocusEffect
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.compose.place.toDiaryMapCoordinate
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagPickerEvent
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.feature.place.api.PlaceAddedResult
import io.github.taetae98coding.diary.feature.place.ui.Res
import io.github.taetae98coding.diary.feature.place.ui.form.PlaceFormState
import io.github.taetae98coding.diary.feature.place.ui.form.handlePlaceFormEvent
import io.github.taetae98coding.diary.feature.place.ui.form.rememberPlaceAddFormState
import io.github.taetae98coding.diary.feature.place.ui.place_add_succeeded_message
import io.github.taetae98coding.diary.feature.place.ui.place_add_title_blank_message
import io.github.taetae98coding.diary.feature.place.ui.place_coordinate_invalid_message
import io.github.taetae98coding.diary.feature.place.ui.search.PlaceSearchEvent
import io.github.taetae98coding.diary.feature.place.ui.search.PlaceSearchViewModel
import io.github.taetae98coding.diary.feature.place.ui.tag.PlaceTagAddedResultEffect
import io.github.taetae98coding.diary.library.compose.ui.color.randomColor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.uuid.Uuid

@Composable
internal fun PlaceAddScreen(
    navigateUp: () -> Unit,
    navigateToTagAdd: () -> Unit,
    navigateToTagDetail: (Uuid) -> Unit,
    tagAddRequestKey: Uuid,
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
    AddEffect(
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
                tagPagingItems = tagPagingItems,
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
                tagIdSet = tagViewModel.tagIdSet.value,
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

@Composable
internal fun AddEffect(
    resultEventBus: ResultEventBus = LocalResultEventBus.current,
    effect: Flow<PlaceAddEffect> = emptyFlow(),
    scaffoldState: PlaceFormState = rememberPlaceAddFormState(),
) {
    val coroutineScope = rememberCoroutineScope()
    val addSucceededMessage = stringResource(Res.string.place_add_succeeded_message)
    val titleBlankMessage = stringResource(Res.string.place_add_title_blank_message)
    val coordinateInvalidMessage = stringResource(Res.string.place_coordinate_invalid_message)

    CollectEffect(effect) { value ->
        when (value) {
            is PlaceAddEffect.AddSucceeded -> {
                resultEventBus.sendResult<PlaceAddedResult>(result = PlaceAddedResult(id = value.id))
                scaffoldState.titleState.clearText()
                scaffoldState.descriptionState.clearText()
                scaffoldState.addressState.clearText()
                scaffoldState.clearCoordinate()
                scaffoldState.mapState.selectSpot(null)
                scaffoldState.titleState.requestFocus()
                coroutineScope.launch { scaffoldState.colorState.animateTo(color = randomColor()) }
                coroutineScope.launch { scaffoldState.hostState.showImmediate(message = addSucceededMessage) }
            }

            is PlaceAddEffect.TitleBlank -> {
                scaffoldState.titleState.requestFocus()
                coroutineScope.launch { scaffoldState.hostState.showImmediate(message = titleBlankMessage) }
            }

            is PlaceAddEffect.CoordinateInvalid -> {
                coroutineScope.launch { scaffoldState.hostState.showImmediate(message = coordinateInvalidMessage) }
            }
        }
    }
}
