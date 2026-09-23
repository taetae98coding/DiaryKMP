package io.github.taetae98coding.diary.feature.place.ui.search

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProvider
import io.github.taetae98coding.diary.core.model.place.SearchedPlace
import io.github.taetae98coding.diary.feature.place.ui.Res
import io.github.taetae98coding.diary.feature.place.ui.place_search_empty_message
import io.github.taetae98coding.diary.feature.place.ui.place_search_failed_message
import io.github.taetae98coding.diary.feature.place.ui.previewSearchedPlace
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PlaceSearchResult(
    state: PlaceSearchDialogState,
    onSelect: (SearchedPlace) -> Unit,
    modifier: Modifier = Modifier,
    uiStateProvider: () -> PlaceSearchUiState = { PlaceSearchUiState.Idle },
) {
    val uiState = uiStateProvider()

    if (!state.hasQuery || uiState is PlaceSearchUiState.Idle) {
        Box(modifier = modifier)
        return
    }

    when (uiState) {
        is PlaceSearchUiState.Idle -> Box(modifier = modifier)

        is PlaceSearchUiState.Failed ->
            PlaceSearchMessage(
                message = stringResource(Res.string.place_search_failed_message),
                modifier = modifier,
            )

        is PlaceSearchUiState.Loaded ->
            if (uiState.placeList.isEmpty()) {
                PlaceSearchMessage(
                    message = stringResource(Res.string.place_search_empty_message),
                    modifier = modifier,
                )
            } else {
                PlaceSearchList(
                    placeList = uiState.placeList,
                    onSelect = onSelect,
                    modifier = modifier,
                )
            }
    }
}

@Composable
private fun PlaceSearchMessage(
    message: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@ScreenPreview
@Composable
private fun PlaceSearchResultPreview() {
    val placeList = remember { listOf(previewSearchedPlace(name = "서울시청", latitude = 37.5665, longitude = 126.9780)) }

    DiaryTheme {
        Surface {
            PlaceSearchResult(
                state = rememberPlaceSearchDialogState(initialProvider = DiaryMapProvider.NAVER),
                onSelect = {},
                uiStateProvider = { PlaceSearchUiState.Loaded(placeList = placeList) },
            )
        }
    }
}
