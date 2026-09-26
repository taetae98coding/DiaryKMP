package io.github.taetae98coding.diary.feature.place.ui.detail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.ViewModelStoreProvider
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreProvider
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.animation.DiaryScaleVisibility
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import io.github.taetae98coding.diary.compose.core.snackbar.DismissUndoSnackbarEffect
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagInputUiState
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagPickerEvent
import io.github.taetae98coding.diary.core.model.place.PlaceDetail
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.place.toPlacePrecision
import io.github.taetae98coding.diary.feature.place.ui.Res
import io.github.taetae98coding.diary.feature.place.ui.detail.memo.PlaceDetailMemoContent
import io.github.taetae98coding.diary.feature.place.ui.detail.memo.PlaceDetailMemoFloatingActionButton
import io.github.taetae98coding.diary.feature.place.ui.detail.tab.PlaceDetailTab
import io.github.taetae98coding.diary.feature.place.ui.detail.tab.placeDetailTabShortcut
import io.github.taetae98coding.diary.feature.place.ui.detail.tab.rememberPlaceDetailTabState
import io.github.taetae98coding.diary.feature.place.ui.form.PlaceFormState
import io.github.taetae98coding.diary.feature.place.ui.form.handlePlaceFormEvent
import io.github.taetae98coding.diary.feature.place.ui.form.rememberPlaceDetailFormState
import io.github.taetae98coding.diary.feature.place.ui.place_coordinate_invalid_message
import io.github.taetae98coding.diary.feature.place.ui.place_detail_update_succeeded_message
import io.github.taetae98coding.diary.feature.place.ui.search.PlaceSearchEvent
import io.github.taetae98coding.diary.feature.place.ui.search.PlaceSearchViewModel
import io.github.taetae98coding.diary.feature.place.ui.tag.PlaceTagAddedResultEffect
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
    navigateToMemoAdd: () -> Unit,
    navigateToMemoDetail: (Uuid) -> Unit,
    id: Uuid,
    tagAddRequestKey: Uuid,
    detailViewModel: PlaceDetailViewModel,
    searchViewModel: PlaceSearchViewModel,
    tagViewModel: PlaceDetailTagViewModel,
    modifier: Modifier = Modifier,
) {
    val tabState = key(id) { rememberPlaceDetailTabState() }
    val viewModelStoreProvider = rememberViewModelStoreProvider()
    val uiState by detailViewModel.uiState.collectAsStateWithLifecycle()
    val searchUiState by searchViewModel.uiState.collectAsStateWithLifecycle()
    val tagUiState by tagViewModel.uiState.collectAsStateWithLifecycle()
    val tagPagingItems = tagViewModel.tagPagingData.collectAsLazyPagingItems()
    val selectableTagPagingItems = tagViewModel.selectableTagPagingData.collectAsLazyPagingItems()
    val content = uiState as? PlaceDetailUiState.Content
    val scaffoldState = key(content?.id) { rememberPlaceDetailFormState(initialDetail = content?.detail ?: PlaceDetail.EMPTY, defaultProvider = content?.defaultProvider) }
    val isUpdateEnabled by rememberIsUpdateEnabled(scaffoldState = scaffoldState, uiStateProvider = { uiState })
    val openExternalMap = rememberOpenExternalMap(scaffoldState = scaffoldState, savedTitleProvider = { content?.detail?.title.orEmpty() })

    PlaceTagAddedResultEffect(requestKey = tagAddRequestKey, onTagAdded = tagViewModel::add)
    PlaceDetailScreenEffect(effect = detailViewModel.effect, scaffoldState = scaffoldState, navigateUp = navigateUp)
    DismissUndoSnackbarEffect(keyProvider = { tabState.tab }, hostState = scaffoldState.hostState)

    PlaceDetailScaffold(
        state = scaffoldState,
        onEvent = { event -> handlePlaceDetailScaffoldEvent(event = event, detailViewModel = detailViewModel, scaffoldState = scaffoldState, openExternalMap = openExternalMap, navigateUp = navigateUp) },
        onSearchEvent = { event -> searchViewModel.handleSearchEvent(event = event, scaffoldState = scaffoldState) },
        onTagPickerEvent = { event -> tagViewModel.handleTagPickerEvent(event = event, navigateToTagAdd = navigateToTagAdd) },
        modifier =
            modifier.placeDetailTabShortcut(
                onUpdate = { detailViewModel.update(detail = scaffoldState.detail) },
                onMemoAdd = navigateToMemoAdd,
                state = tabState,
                isUpdateEnabledProvider = { isUpdateEnabled },
            ),
        tabState = tabState,
        tagPagingItems = tagPagingItems,
        uiStateProvider = { uiState },
        searchUiStateProvider = { searchUiState },
        tagUiStateProvider = { tagUiState },
        tabFloatingActionButton = { tab ->
            TabFloatingActionButton(
                tab = tab,
                onUpdate = { detailViewModel.update(detail = scaffoldState.detail) },
                onMemoAdd = navigateToMemoAdd,
                isUpdateVisible = isUpdateEnabled,
                isUpdateInProgressProvider = { content?.isUpdateInProgress == true },
            )
        },
    ) { tab ->
        TabContent(
            tab = tab,
            id = id,
            viewModelStoreProvider = viewModelStoreProvider,
            navigateToTagAdd = navigateToTagAdd,
            navigateToTagDetail = navigateToTagDetail,
            navigateToMemoDetail = navigateToMemoDetail,
            state = scaffoldState,
            selectableTagPagingItems = selectableTagPagingItems,
            uiStateProvider = { uiState },
            tagUiStateProvider = { tagUiState },
        )
    }
}

@Composable
private fun rememberIsUpdateEnabled(
    scaffoldState: PlaceFormState,
    uiStateProvider: () -> PlaceDetailUiState,
): State<Boolean> =
    remember(scaffoldState) {
        derivedStateOf {
            val loaded = uiStateProvider() as? PlaceDetailUiState.Content
            loaded != null && scaffoldState.detail != loaded.detail.copy(coordinate = loaded.detail.coordinate.toPlacePrecision())
        }
    }

@Composable
private fun TabFloatingActionButton(
    tab: PlaceDetailTab,
    onUpdate: () -> Unit,
    onMemoAdd: () -> Unit,
    isUpdateVisible: Boolean = false,
    isUpdateInProgressProvider: () -> Boolean = { false },
) {
    when (tab) {
        PlaceDetailTab.DETAIL ->
            DiaryScaleVisibility(visible = isUpdateVisible) {
                PlaceDetailUpdateFloatingActionButton(
                    onClick = onUpdate,
                    isInProgressProvider = isUpdateInProgressProvider,
                )
            }

        PlaceDetailTab.MEMO -> PlaceDetailMemoFloatingActionButton(onClick = onMemoAdd)
    }
}

@Composable
private fun TabContent(
    tab: PlaceDetailTab,
    id: Uuid,
    viewModelStoreProvider: ViewModelStoreProvider,
    navigateToTagAdd: () -> Unit,
    navigateToTagDetail: (Uuid) -> Unit,
    navigateToMemoDetail: (Uuid) -> Unit,
    state: PlaceFormState,
    selectableTagPagingItems: LazyPagingItems<Tag>,
    uiStateProvider: () -> PlaceDetailUiState = { PlaceDetailUiState.Loading },
    tagUiStateProvider: () -> EntityTagInputUiState = { EntityTagInputUiState() },
) {
    when (tab) {
        PlaceDetailTab.DETAIL ->
            PlaceDetailScaffoldContent(
                onFormEvent = { event ->
                    handlePlaceFormEvent(
                        event = event,
                        state = state,
                        selectableTagPagingItems = selectableTagPagingItems,
                        navigateToTagAdd = navigateToTagAdd,
                        navigateToTagDetail = navigateToTagDetail,
                    )
                },
                state = state,
                modifier = Modifier.fillMaxSize(),
                uiStateProvider = uiStateProvider,
                tagUiStateProvider = tagUiStateProvider,
            )

        PlaceDetailTab.MEMO ->
            PlaceDetailMemoContent(
                id = id,
                viewModelStoreProvider = viewModelStoreProvider,
                navigateToMemoDetail = navigateToMemoDetail,
                modifier = Modifier.fillMaxSize(),
                snackbarHostState = state.hostState,
            )
    }
}

private fun handlePlaceDetailScaffoldEvent(
    event: PlaceDetailScaffoldEvent,
    detailViewModel: PlaceDetailViewModel,
    scaffoldState: PlaceFormState,
    openExternalMap: () -> Unit,
    navigateUp: () -> Unit,
) {
    when (event) {
        is PlaceDetailScaffoldEvent.ClickNavigateUp -> navigateUp()
        is PlaceDetailScaffoldEvent.ClickUpdate -> detailViewModel.update(detail = scaffoldState.detail)
        is PlaceDetailScaffoldEvent.ClickDelete -> detailViewModel.delete()
        is PlaceDetailScaffoldEvent.ClickSearch -> scaffoldState.searchDialogState.show()
        is PlaceDetailScaffoldEvent.ClickOpenExternalMap -> openExternalMap()
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

@Composable
private fun rememberOpenExternalMap(
    scaffoldState: PlaceFormState,
    savedTitleProvider: () -> String,
): () -> Unit {
    val externalMapOpener = rememberExternalMapOpener()
    val coroutineScope = rememberCoroutineScope()
    val coordinateInvalidMessage = stringResource(Res.string.place_coordinate_invalid_message)
    val latestSavedTitleProvider by rememberUpdatedState(savedTitleProvider)

    return remember(scaffoldState, externalMapOpener, coroutineScope, coordinateInvalidMessage) {
        {
            val coordinate = scaffoldState.spot

            if (coordinate == null) {
                coroutineScope.launch { scaffoldState.hostState.showImmediate(message = coordinateInvalidMessage) }
            } else {
                externalMapOpener.open(
                    provider = scaffoldState.mapState.provider,
                    coordinate = coordinate,
                    title = externalMapTitle(inputTitle = scaffoldState.detail.title, savedTitle = latestSavedTitleProvider()),
                    address = scaffoldState.detail.address,
                )
            }
        }
    }
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
