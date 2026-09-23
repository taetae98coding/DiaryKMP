package io.github.taetae98coding.diary.feature.tag.ui.detail.place

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
import io.github.taetae98coding.diary.feature.tag.ui.previewPlace

@Composable
internal fun TagDetailPlaceMapContent(
    onEvent: (TagDetailPlaceContentEvent) -> Unit,
    modifier: Modifier = Modifier,
    mapState: DiaryMapState = rememberDiaryMapState(),
    placeListUiStateProvider: () -> TagDetailPlaceListUiState = { TagDetailPlaceListUiState() },
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
                onPinClick = { id -> onEvent(TagDetailPlaceContentEvent.ClickPlace(id = id)) },
            )
        },
        secondary = {
            TagDetailPlaceBoundsList(
                onEvent = onEvent,
                modifier = Modifier.fillMaxSize(),
                placeListUiStateProvider = placeListUiStateProvider,
                isRefreshingProvider = isRefreshingProvider,
                sortProvider = sortProvider,
            )
        },
        modifier = modifier.fillMaxSize(),
    )
}

@ScreenPreview
@Composable
private fun TagDetailPlaceMapContentPreview() {
    val placeList = remember { listOf(previewPlace(title = "집", color = 0xFF3A7BD5, latitude = 37.5665, longitude = 126.9780)) }

    DiaryTheme {
        Surface {
            TagDetailPlaceMapContent(
                onEvent = {},
                placeListUiStateProvider = { TagDetailPlaceListUiState(isLoaded = true, placeList = placeList) },
            )
        }
    }
}
