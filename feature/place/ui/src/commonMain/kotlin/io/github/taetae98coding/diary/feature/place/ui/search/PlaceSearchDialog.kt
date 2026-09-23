@file:OptIn(ExperimentalMaterial3Api::class)

package io.github.taetae98coding.diary.feature.place.ui.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProvider
import io.github.taetae98coding.diary.core.model.place.SearchedPlace
import io.github.taetae98coding.diary.feature.place.ui.Res
import io.github.taetae98coding.diary.feature.place.ui.place_search_title
import io.github.taetae98coding.diary.feature.place.ui.previewSearchedPlace
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PlaceSearchDialog(
    state: PlaceSearchDialogState,
    onSelect: (SearchedPlace) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    uiStateProvider: () -> PlaceSearchUiState = { PlaceSearchUiState.Idle },
) {
    val contentDescription = stringResource(Res.string.place_search_title)

    PlaceSearchFocusEffect(state = state)

    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
    ) {
        Surface(
            modifier =
                Modifier
                    .fillMaxHeight()
                    .semantics { this.contentDescription = contentDescription },
            shape = AlertDialogDefaults.shape,
            color = AlertDialogDefaults.containerColor,
            tonalElevation = AlertDialogDefaults.TonalElevation,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(PlaceSearchDialogDefaults.MAP_HEIGHT_RATIO),
                ) {
                    PlaceSearchMap(
                        state = state,
                        uiStateProvider = uiStateProvider,
                        onSelect = onSelect,
                    )
                }
                PlaceSearchResult(
                    state = state,
                    uiStateProvider = uiStateProvider,
                    onSelect = onSelect,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .weight(1F),
                )
                PlaceSearchQueryInput(
                    state = state,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(DiaryTheme.dimens.screenPaddingValues),
                )
            }
        }
    }
}

@ScreenPreview
@Composable
private fun PlaceSearchDialogPreview() {
    val placeList = remember { listOf(previewSearchedPlace(name = "서울시청", latitude = 37.5665, longitude = 126.9780)) }

    DiaryTheme {
        PlaceSearchDialog(
            state = rememberPlaceSearchDialogState(initialProvider = DiaryMapProvider.NAVER),
            onSelect = {},
            onDismissRequest = {},
            uiStateProvider = { PlaceSearchUiState.Loaded(placeList = placeList) },
        )
    }
}
