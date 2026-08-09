package io.github.taetae98coding.diary.feature.place.ui.search

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.map.DiaryMap
import io.github.taetae98coding.diary.compose.map.DiaryMapPin
import io.github.taetae98coding.diary.compose.map.DiaryMapProvider
import io.github.taetae98coding.diary.compose.place.toDiaryMapCoordinate
import io.github.taetae98coding.diary.core.model.place.SearchedPlace
import io.github.taetae98coding.diary.feature.place.ui.Res
import io.github.taetae98coding.diary.feature.place.ui.place_search_map_content_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PlaceSearchMap(
    state: PlaceSearchDialogState,
    onSelect: (SearchedPlace) -> Unit,
    modifier: Modifier = Modifier,
    uiStateProvider: () -> PlaceSearchUiState = { PlaceSearchUiState.Idle },
) {
    val contentDescription = stringResource(Res.string.place_search_map_content_description)

    ReflectPinEffect(
        state = state,
        placeListProvider = { uiStateProvider().placeList },
    )

    DiaryMap(
        state = state.mapState,
        modifier =
            modifier
                .fillMaxSize()
                .semantics { this.contentDescription = contentDescription },
        onPinClick = { id -> uiStateProvider().placeList.firstOrNull { place -> place.id == id }?.let(onSelect) },
    )
}

@Composable
private fun ReflectPinEffect(
    state: PlaceSearchDialogState,
    placeListProvider: () -> List<SearchedPlace> = { emptyList() },
) {
    val color = MaterialTheme.colorScheme.primary
    val latestPlaceListProvider by rememberUpdatedState(placeListProvider)

    LaunchedEffect(state, color) {
        snapshotFlow { latestPlaceListProvider() }
            .collect { placeList ->
                state.mapState.updatePins(placeList.map { place -> place.toDiaryMapPin(color = color) })
            }
    }
}

private fun SearchedPlace.toDiaryMapPin(color: Color): DiaryMapPin =
    DiaryMapPin(
        id = id,
        coordinate = coordinate.toDiaryMapCoordinate(),
        color = color,
        label = name,
    )

internal val PlaceSearchUiState.placeList: List<SearchedPlace>
    get() = (this as? PlaceSearchUiState.Loaded)?.placeList.orEmpty()

@ScreenPreview
@Composable
private fun PlaceSearchMapPreview() {
    DiaryTheme {
        PlaceSearchMap(
            state = rememberPlaceSearchDialogState(initialProvider = DiaryMapProvider.NAVER),
            onSelect = {},
        )
    }
}
