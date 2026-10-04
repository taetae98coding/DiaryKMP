package io.github.taetae98coding.diary.feature.memo.ui.place

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.dialog.DiaryPagingPickerDialog
import io.github.taetae98coding.diary.compose.core.dialog.DiaryPagingPickerText
import io.github.taetae98coding.diary.compose.core.dialog.DiaryPickerSearchFieldState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDiaryPickerSearchFieldState
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
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
    searchFieldState: DiaryPickerSearchFieldState = rememberDiaryPickerSearchFieldState(),
    placePagingItems: LazyPagingItems<Place> = remember { flowOf(PagingData.empty<Place>()) }.collectAsLazyPagingItems(),
    uiStateProvider: () -> MemoPlaceInputUiState = { MemoPlaceInputUiState() },
    coordinateProvider: () -> Coordinate? = { null },
) {
    DiaryPagingPickerDialog(
        text =
            DiaryPagingPickerText(
                title = stringResource(Res.string.memo_place_picker_title),
                searchPlaceholder = stringResource(Res.string.memo_place_picker_search_placeholder),
                searchEmptyTitle = stringResource(Res.string.memo_place_picker_search_empty_title),
                searchEmptyDescription = stringResource(Res.string.memo_place_picker_search_empty_description),
                addLabel = stringResource(Res.string.memo_place_picker_add_label),
                addActionLabel = stringResource(Res.string.memo_place_add_action),
            ),
        onAddClick = { onEvent(MemoPlacePickerEvent.ClickAdd(coordinate = coordinateProvider())) },
        onDismissRequest = onDismissRequest,
        itemKey = { place -> place.id },
        modifier = modifier,
        searchFieldState = searchFieldState,
        pagingItems = placePagingItems,
        isSearchFocusRequested = true,
    ) { place, itemModifier ->
        MemoPlacePickerRow(
            onEvent = onEvent,
            modifier = itemModifier,
            place = place,
            isSelected = place != null && uiStateProvider().selectedPlaceList.any { selectedPlace -> selectedPlace.id == place.id },
        )
    }
}

@ScreenPreview
@Composable
private fun MemoPlacePickerDialogPreview() {
    val placeList = remember { listOf(previewPlace(title = "집", color = 0xFF3A7BD5, latitude = 37.5665, longitude = 126.9780)) }
    val placePagingData = remember(placeList) { flowOf(PagingData.from(placeList)) }

    DiaryTheme {
        MemoPlacePickerDialog(
            onDismissRequest = {},
            onEvent = {},
            placePagingItems = placePagingData.collectAsLazyPagingItems(),
            uiStateProvider = { MemoPlaceInputUiState(isSelectedPlaceLoaded = true, selectedPlaceList = placeList) },
        )
    }
}
