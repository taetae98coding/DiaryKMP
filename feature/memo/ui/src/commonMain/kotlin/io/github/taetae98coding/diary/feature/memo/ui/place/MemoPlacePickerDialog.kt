package io.github.taetae98coding.diary.feature.memo.ui.place

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.dialog.DiaryPagingPickerBody
import io.github.taetae98coding.diary.compose.core.dialog.DiaryPagingPickerText
import io.github.taetae98coding.diary.compose.core.dialog.DiaryPickerAddButton
import io.github.taetae98coding.diary.compose.core.dialog.DiaryPickerDialog
import io.github.taetae98coding.diary.compose.core.dialog.DiaryPickerSearchFieldState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDiaryPickerSearchFieldState
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.compose.map.rememberDiaryMapState
import io.github.taetae98coding.diary.compose.place.previewPlace
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.feature.memo.ui.Res
import io.github.taetae98coding.diary.feature.memo.ui.memo_place_add_action
import io.github.taetae98coding.diary.feature.memo.ui.memo_place_picker_add_label
import io.github.taetae98coding.diary.feature.memo.ui.memo_place_picker_search_empty_description
import io.github.taetae98coding.diary.feature.memo.ui.memo_place_picker_search_empty_title
import io.github.taetae98coding.diary.feature.memo.ui.memo_place_picker_search_placeholder
import io.github.taetae98coding.diary.feature.memo.ui.memo_place_picker_title
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MemoPlacePickerDialog(
    onDismissRequest: () -> Unit,
    onEvent: (MemoPlacePickerEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: MemoPlacePickerState = rememberMemoPlacePickerState(),
    searchFieldState: DiaryPickerSearchFieldState = rememberDiaryPickerSearchFieldState(),
    mapState: DiaryMapState? = null,
    placePagingItems: LazyPagingItems<Place> = remember { flowOf(PagingData.empty<Place>()) }.collectAsLazyPagingItems(),
    uiStateProvider: () -> MemoPlaceInputUiState = { MemoPlaceInputUiState() },
    mapUiStateProvider: () -> MemoPlacePickerMapUiState = { MemoPlacePickerMapUiState() },
    coordinateProvider: () -> Coordinate? = { null },
) {
    val text =
        DiaryPagingPickerText(
            title = stringResource(Res.string.memo_place_picker_title),
            searchPlaceholder = stringResource(Res.string.memo_place_picker_search_placeholder),
            searchEmptyTitle = stringResource(Res.string.memo_place_picker_search_empty_title),
            searchEmptyDescription = stringResource(Res.string.memo_place_picker_search_empty_description),
            addLabel = stringResource(Res.string.memo_place_picker_add_label),
            addActionLabel = stringResource(Res.string.memo_place_add_action),
        )

    DiaryPickerDialog(
        title = text.title,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        titleAction = mapState?.let { { MemoPlacePickerViewModeButton(state = state) } },
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            MemoPlacePickerDialogBody(
                onEvent = onEvent,
                text = text,
                state = state,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(weight = 1F, fill = false),
                searchFieldState = searchFieldState,
                mapState = mapState,
                placePagingItems = placePagingItems,
                uiStateProvider = uiStateProvider,
                mapUiStateProvider = mapUiStateProvider,
            )
            DiaryPickerAddButton(
                onClick = { onEvent(MemoPlacePickerEvent.ClickAdd(coordinate = state.addCoordinate(mapState = mapState, cardCoordinate = coordinateProvider()))) },
                label = text.addLabel,
                actionLabel = text.addActionLabel,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun MemoPlacePickerDialogBody(
    onEvent: (MemoPlacePickerEvent) -> Unit,
    text: DiaryPagingPickerText,
    state: MemoPlacePickerState,
    modifier: Modifier = Modifier,
    searchFieldState: DiaryPickerSearchFieldState = rememberDiaryPickerSearchFieldState(),
    mapState: DiaryMapState? = null,
    placePagingItems: LazyPagingItems<Place> = remember { flowOf(PagingData.empty<Place>()) }.collectAsLazyPagingItems(),
    uiStateProvider: () -> MemoPlaceInputUiState = { MemoPlaceInputUiState() },
    mapUiStateProvider: () -> MemoPlacePickerMapUiState = { MemoPlacePickerMapUiState() },
) {
    DiaryCrossfade(
        targetState = state.shownViewMode(mapState = mapState),
        modifier = modifier,
    ) { viewMode ->
        when (viewMode) {
            MemoPlacePickerViewMode.LIST ->
                DiaryPagingPickerBody(
                    text = text,
                    itemKey = { place -> place.id },
                    modifier = Modifier.fillMaxWidth(),
                    searchFieldState = searchFieldState,
                    pagingItems = placePagingItems,
                    isSearchFocusRequested = !state.isMapViewVisited,
                ) { place, itemModifier ->
                    MemoPlacePickerRow(
                        onEvent = onEvent,
                        modifier = itemModifier,
                        place = place,
                        isSelected = place != null && uiStateProvider().selectedPlaceList.any { selectedPlace -> selectedPlace.id == place.id },
                    )
                }

            MemoPlacePickerViewMode.MAP ->
                mapState?.let { value ->
                    MemoPlacePickerMapBody(
                        onEvent = onEvent,
                        mapState = value,
                        modifier = Modifier.fillMaxWidth(),
                        uiStateProvider = uiStateProvider,
                        mapUiStateProvider = mapUiStateProvider,
                    )
                }
        }
    }
}

private class MemoPlacePickerViewModeDialogPreviewParameter : PreviewParameterProvider<MemoPlacePickerViewMode> {
    override val values: Sequence<MemoPlacePickerViewMode> = sequenceOf(MemoPlacePickerViewMode.LIST, MemoPlacePickerViewMode.MAP)
}

@ScreenPreview
@Composable
private fun MemoPlacePickerDialogPreview(
    @PreviewParameter(MemoPlacePickerViewModeDialogPreviewParameter::class) viewMode: MemoPlacePickerViewMode,
) {
    val placeList = remember { listOf(previewPlace(title = "집", color = 0xFF3A7BD5, latitude = 37.5665, longitude = 126.9780)) }
    val placePagingData = remember(placeList) { flowOf(PagingData.from(placeList)) }

    DiaryTheme {
        MemoPlacePickerDialog(
            onDismissRequest = {},
            onEvent = {},
            state = MemoPlacePickerState(initialViewMode = viewMode),
            mapState = rememberDiaryMapState(),
            placePagingItems = placePagingData.collectAsLazyPagingItems(),
            uiStateProvider = { MemoPlaceInputUiState(isSelectedPlaceLoaded = true, selectedPlaceList = placeList) },
            mapUiStateProvider = { MemoPlacePickerMapUiState(isLoaded = true, placeList = placeList) },
        )
    }
}
