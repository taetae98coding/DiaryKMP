package io.github.taetae98coding.diary.feature.memo.ui.place

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDiaryPickerSearchFieldState
import io.github.taetae98coding.diary.compose.core.effect.DiarySearchQueryEffect
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.feature.memo.ui.previewPlace
import kotlinx.coroutines.flow.flowOf

@Composable
internal fun MemoPlacePickerDialogHost(
    dialogState: DialogState,
    onEvent: (MemoPlacePickerEvent) -> Unit,
    uiStateProvider: () -> MemoPlaceInputUiState = { MemoPlaceInputUiState() },
    placePagingItems: LazyPagingItems<Place> = remember { flowOf(PagingData.empty<Place>()) }.collectAsLazyPagingItems(),
    coordinateProvider: () -> Coordinate? = { null },
) {
    if (!dialogState.isVisible) return

    val searchFieldState = rememberDiaryPickerSearchFieldState()

    val hide = {
        onEvent(MemoPlacePickerEvent.ChangeQuery(query = ""))
        dialogState.hide()
    }

    DiarySearchQueryEffect(
        queryState = searchFieldState.textFieldState,
        onQueryChange = { query -> onEvent(MemoPlacePickerEvent.ChangeQuery(query = query)) },
    )

    MemoPlacePickerDialog(
        onDismissRequest = hide,
        onEvent = { event ->
            if (event is MemoPlacePickerEvent.ClickAdd) hide()
            onEvent(event)
        },
        searchFieldState = searchFieldState,
        uiStateProvider = uiStateProvider,
        placePagingItems = placePagingItems,
        coordinateProvider = coordinateProvider,
    )
}

@ScreenPreview
@Composable
private fun MemoPlacePickerDialogHostPreview() {
    val placeList = remember { listOf(previewPlace(title = "집", color = 0xFF3A7BD5, latitude = 37.5665, longitude = 126.9780)) }
    val placePagingData = remember(placeList) { flowOf(PagingData.from(placeList)) }

    DiaryTheme {
        MemoPlacePickerDialogHost(
            dialogState = rememberDialogState().apply { show() },
            onEvent = {},
            uiStateProvider = { MemoPlaceInputUiState(isSelectedPlaceLoaded = true, selectedPlaceList = placeList) },
            placePagingItems = placePagingData.collectAsLazyPagingItems(),
        )
    }
}
