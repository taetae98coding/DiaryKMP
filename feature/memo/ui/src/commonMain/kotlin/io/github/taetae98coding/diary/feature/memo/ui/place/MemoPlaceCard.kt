package io.github.taetae98coding.diary.feature.memo.ui.place

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Card
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.compose.place.toDiaryMapCoordinate
import io.github.taetae98coding.diary.feature.memo.ui.previewPlace
import kotlin.uuid.Uuid

private val MapHeight = 240.dp

@Composable
internal fun MemoPlaceCard(
    onPlaceClick: (Uuid) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
    mapState: DiaryMapState? = null,
    uiStateProvider: () -> MemoPlaceCardUiState = { MemoPlaceCardUiState() },
) {
    Card(modifier = modifier) {
        MemoPlaceMapBox(
            onPinClick = onPlaceClick,
            modifier = Modifier.height(MapHeight),
            mapState = mapState,
            placeListProvider = { uiStateProvider().placeUiState.selectedPlaceList },
        )
        MemoPlaceFlexBox(
            onShowOnMap = { coordinate -> mapState?.moveTo(coordinate.toDiaryMapCoordinate()) },
            onAddClick = onAddClick,
            placeListProvider = { uiStateProvider().placeUiState.selectedPlaceList },
        )
    }
}

@Composable
internal fun MemoPlaceFillHeightCard(
    onPlaceClick: (Uuid) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
    mapState: DiaryMapState? = null,
    uiStateProvider: () -> MemoPlaceCardUiState = { MemoPlaceCardUiState() },
) {
    Card(modifier = modifier) {
        MemoPlaceMapBox(
            onPinClick = onPlaceClick,
            modifier = Modifier.weight(1F),
            mapState = mapState,
            placeListProvider = { uiStateProvider().placeUiState.selectedPlaceList },
        )
        MemoPlaceScrollableFlexBox(
            onShowOnMap = { coordinate -> mapState?.moveTo(coordinate.toDiaryMapCoordinate()) },
            onAddClick = onAddClick,
            placeListProvider = { uiStateProvider().placeUiState.selectedPlaceList },
        )
    }
}

private class MemoPlaceCardUiStatePreviewParameter : PreviewParameterProvider<MemoPlaceCardUiState> {
    override val values: Sequence<MemoPlaceCardUiState>
        get() {
            val placeList =
                listOf(
                    previewPlace(title = "집", color = 0xFF3A7BD5, latitude = 37.5665, longitude = 126.9780),
                    previewPlace(title = "회사", color = 0xFFE57373, latitude = 37.3595, longitude = 127.1052),
                )

            return sequenceOf(
                MemoPlaceCardUiState(),
                MemoPlaceCardUiState(
                    placeUiState = MemoPlaceInputUiState(isSelectedPlaceLoaded = true, selectedPlaceList = placeList),
                ),
            )
        }
}

@ComponentPreview
@Composable
private fun MemoPlaceCardPreview(
    @PreviewParameter(MemoPlaceCardUiStatePreviewParameter::class) uiState: MemoPlaceCardUiState,
) {
    DiaryTheme {
        Surface {
            MemoPlaceCard(
                uiStateProvider = { uiState },
                onPlaceClick = {},
                onAddClick = {},
            )
        }
    }
}

@ScreenPreview
@Composable
private fun MemoPlaceFillHeightCardPreview(
    @PreviewParameter(MemoPlaceCardUiStatePreviewParameter::class) uiState: MemoPlaceCardUiState,
) {
    DiaryTheme {
        Surface {
            MemoPlaceFillHeightCard(
                uiStateProvider = { uiState },
                onPlaceClick = {},
                onAddClick = {},
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
