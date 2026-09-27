package io.github.taetae98coding.diary.feature.place.ui.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.compose.map.rememberDiaryMapState
import io.github.taetae98coding.diary.feature.place.ui.previewSearchedPlace

@Composable
internal fun PlaceSearchHost(
    onEvent: (PlaceSearchEvent) -> Unit,
    uiStateProvider: () -> PlaceSearchUiState = { PlaceSearchUiState.Idle },
    dialogState: DialogState = rememberDialogState(),
    mapState: DiaryMapState = rememberDiaryMapState(),
) {
    if (!dialogState.isVisible) return

    val state = rememberPlaceSearchDialogState(hostMapState = mapState)

    PlaceSearchEffect(
        state = state,
        onSearch = { request -> onEvent(PlaceSearchEvent.Search(request = request)) },
        onClear = { onEvent(PlaceSearchEvent.ClearSearch) },
    )

    PlaceSearchDialog(
        state = state,
        uiStateProvider = uiStateProvider,
        onSelect = { place ->
            onEvent(PlaceSearchEvent.Select(place = place))
            onEvent(PlaceSearchEvent.ClearSearch)
        },
        onDismissRequest = {
            dialogState.hide()
            onEvent(PlaceSearchEvent.ClearSearch)
        },
    )
}

@ScreenPreview
@Composable
private fun PlaceSearchHostPreview() {
    val placeList = remember { listOf(previewSearchedPlace(name = "서울시청", latitude = 37.5665, longitude = 126.9780)) }

    DiaryTheme {
        PlaceSearchHost(
            onEvent = {},
            uiStateProvider = { PlaceSearchUiState.Loaded(placeList = placeList) },
            dialogState = rememberDialogState().apply { show() },
        )
    }
}
