package io.github.taetae98coding.diary.feature.place.ui.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.layout.DiaryAdaptiveSplitLayout
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.map.DiaryMap
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.compose.map.rememberDiaryMapState
import io.github.taetae98coding.diary.compose.place.PlacePinMarkerEffect
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.feature.place.ui.home.list.PlaceHomePlaceListUiState
import io.github.taetae98coding.diary.feature.place.ui.home.list.PlaceList
import io.github.taetae98coding.diary.feature.place.ui.previewPlace

@Composable
internal fun PlaceHomeContent(
    onEvent: (PlaceHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    mapState: DiaryMapState = rememberDiaryMapState(),
    placeListUiStateProvider: () -> PlaceHomePlaceListUiState = { PlaceHomePlaceListUiState() },
    isRefreshingProvider: () -> Boolean = { false },
    sortProvider: () -> ListSort = { ListSort.TITLE },
) {
    PlacePinMarkerEffect(
        mapState = mapState,
        placeListProvider = { placeListUiStateProvider().placeList },
    )

    DiaryAdaptiveSplitLayout(
        primary = {
            DiaryMap(
                state = mapState,
                onPinClick = { id -> onEvent(PlaceHomeScaffoldEvent.ClickPlace(id = id)) },
            )
        },
        secondary = {
            PlaceList(
                placeListUiStateProvider = placeListUiStateProvider,
                isRefreshingProvider = isRefreshingProvider,
                sortProvider = sortProvider,
                onEvent = onEvent,
            )
        },
        modifier = modifier.fillMaxSize(),
    )
}

@ScreenPreview
@Composable
private fun PlaceHomeContentPreview() {
    val placeList = remember { listOf(previewPlace(title = "집", color = 0xFF3A7BD5, latitude = 37.5665, longitude = 126.9780)) }

    DiaryTheme {
        Surface {
            PlaceHomeContent(
                onEvent = {},
                placeListUiStateProvider = { PlaceHomePlaceListUiState(isLoaded = true, placeList = placeList) },
            )
        }
    }
}
