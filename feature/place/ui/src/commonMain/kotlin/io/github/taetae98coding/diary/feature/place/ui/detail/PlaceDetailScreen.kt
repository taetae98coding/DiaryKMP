package io.github.taetae98coding.diary.feature.place.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.compose.tag.EntityTagPickerEvent
import io.github.taetae98coding.diary.core.model.place.PlaceDetail
import io.github.taetae98coding.diary.feature.place.ui.Res
import io.github.taetae98coding.diary.feature.place.ui.form.PlaceFormState
import io.github.taetae98coding.diary.feature.place.ui.form.handlePlaceFormEvent
import io.github.taetae98coding.diary.feature.place.ui.form.rememberPlaceDetailFormState
import io.github.taetae98coding.diary.feature.place.ui.place_coordinate_invalid_message
import io.github.taetae98coding.diary.feature.place.ui.place_detail_update_succeeded_message
import io.github.taetae98coding.diary.feature.place.ui.search.PlaceSearchEvent
import io.github.taetae98coding.diary.feature.place.ui.search.PlaceSearchViewModel
import io.github.taetae98coding.diary.feature.place.ui.tag.PlaceTagAddedResultEffect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun PlaceDetailScreen(
    navigateUp: () -> Unit,
    navigateToTagAdd: () -> Unit,
    navigateToTagDetail: (Uuid) -> Unit,
    tagAddRequestKey: Uuid,
    detailViewModel: PlaceDetailViewModel,
    searchViewModel: PlaceSearchViewModel,
    tagViewModel: PlaceDetailTagViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by detailViewModel.uiState.collectAsStateWithLifecycle()
    val searchUiState by searchViewModel.uiState.collectAsStateWithLifecycle()
    val tagUiState by tagViewModel.uiState.collectAsStateWithLifecycle()
    val tagPagingItems = tagViewModel.tagPagingData.collectAsLazyPagingItems()
    val content = uiState as? PlaceDetailUiState.Content
    val externalMapOpener = rememberExternalMapOpener()
    val coroutineScope = rememberCoroutineScope()
    val coordinateInvalidMessage = stringResource(Res.string.place_coordinate_invalid_message)

    PlaceTagAddedResultEffect(
        requestKey = tagAddRequestKey,
        onTagAdded = tagViewModel::add,
    )

    key(content?.id) {
        val scaffoldState =
            rememberPlaceDetailFormState(
                initialDetail = content?.detail ?: PlaceDetail.EMPTY,
                defaultProvider = content?.defaultProvider,
            )

        PlaceDetailScreenEffect(
            effect = detailViewModel.effect,
            scaffoldState = scaffoldState,
            navigateUp = navigateUp,
        )

        PlaceDetailScaffold(
            state = scaffoldState,
            tagPagingItems = tagPagingItems,
            uiStateProvider = { uiState },
            searchUiStateProvider = { searchUiState },
            tagUiStateProvider = { tagUiState },
            onEvent = { event ->
                handlePlaceDetailScaffoldEvent(
                    event = event,
                    detailViewModel = detailViewModel,
                    scaffoldState = scaffoldState,
                    externalMapOpener = externalMapOpener,
                    coroutineScope = coroutineScope,
                    savedTitle = content?.detail?.title.orEmpty(),
                    coordinateInvalidMessage = coordinateInvalidMessage,
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
            onSearchEvent = { event -> searchViewModel.handleSearchEvent(event = event, scaffoldState = scaffoldState) },
            onTagPickerEvent = { event -> tagViewModel.handleTagPickerEvent(event = event, navigateToTagAdd = navigateToTagAdd) },
            modifier = modifier,
        )
    }
}

@Suppress("LongParameterList")
private fun handlePlaceDetailScaffoldEvent(
    event: PlaceDetailScaffoldEvent,
    detailViewModel: PlaceDetailViewModel,
    scaffoldState: PlaceFormState,
    externalMapOpener: ExternalMapOpener,
    coroutineScope: CoroutineScope,
    savedTitle: String,
    coordinateInvalidMessage: String,
    navigateUp: () -> Unit,
) {
    when (event) {
        is PlaceDetailScaffoldEvent.ClickNavigateUp -> navigateUp()

        is PlaceDetailScaffoldEvent.ClickUpdate -> detailViewModel.update(detail = scaffoldState.detail)

        is PlaceDetailScaffoldEvent.ClickDelete -> detailViewModel.delete()

        is PlaceDetailScaffoldEvent.ClickSearch -> scaffoldState.searchDialogState.show()

        is PlaceDetailScaffoldEvent.ClickOpenExternalMap ->
            openExternalMapOrNotify(
                scaffoldState = scaffoldState,
                externalMapOpener = externalMapOpener,
                coroutineScope = coroutineScope,
                savedTitle = savedTitle,
                coordinateInvalidMessage = coordinateInvalidMessage,
            )
    }
}

private fun PlaceSearchViewModel.handleSearchEvent(
    event: PlaceSearchEvent,
    scaffoldState: PlaceFormState,
) {
    when (event) {
        is PlaceSearchEvent.Search -> search(request = event.request)
        is PlaceSearchEvent.ClearSearch -> clear()
        is PlaceSearchEvent.Select -> scaffoldState.applySearchedPlace(event.place)
    }
}

private fun PlaceDetailTagViewModel.handleTagPickerEvent(
    event: EntityTagPickerEvent,
    navigateToTagAdd: () -> Unit,
) {
    when (event) {
        is EntityTagPickerEvent.ClickAdd -> navigateToTagAdd()
        is EntityTagPickerEvent.Add -> add(tagId = event.id)
        is EntityTagPickerEvent.Remove -> remove(tagId = event.id)
        is EntityTagPickerEvent.ChangeQuery -> updateQuery(query = event.query)
    }
}

private fun openExternalMapOrNotify(
    scaffoldState: PlaceFormState,
    externalMapOpener: ExternalMapOpener,
    coroutineScope: CoroutineScope,
    savedTitle: String,
    coordinateInvalidMessage: String,
) {
    val coordinate = scaffoldState.spot

    if (coordinate == null) {
        coroutineScope.launch { scaffoldState.hostState.showImmediate(message = coordinateInvalidMessage) }
        return
    }

    externalMapOpener.open(
        provider = scaffoldState.mapState.provider,
        coordinate = coordinate,
        title = externalMapTitle(inputTitle = scaffoldState.detail.title, savedTitle = savedTitle),
        address = scaffoldState.detail.address,
    )
}

@Composable
private fun PlaceDetailScreenEffect(
    scaffoldState: PlaceFormState,
    navigateUp: () -> Unit,
    effect: Flow<PlaceDetailEffect> = emptyFlow(),
) {
    val coroutineScope = rememberCoroutineScope()
    val updateSucceededMessage = stringResource(Res.string.place_detail_update_succeeded_message)
    val coordinateInvalidMessage = stringResource(Res.string.place_coordinate_invalid_message)

    CollectEffect(effect) { value ->
        when (value) {
            is PlaceDetailEffect.UpdateSucceeded -> {
                coroutineScope.launch { scaffoldState.hostState.showImmediate(message = updateSucceededMessage) }
            }

            is PlaceDetailEffect.CoordinateInvalid -> {
                coroutineScope.launch { scaffoldState.hostState.showImmediate(message = coordinateInvalidMessage) }
            }

            is PlaceDetailEffect.DeleteSucceeded -> navigateUp()
        }
    }
}
