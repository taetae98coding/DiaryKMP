package io.github.taetae98coding.diary.feature.memo.ui.place

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.dialog.DiaryPickerEmptyBox
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.map.DiaryMap
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.compose.map.rememberDiaryMapState
import io.github.taetae98coding.diary.compose.place.PlacePinMarkerEffect
import io.github.taetae98coding.diary.compose.place.previewPlace
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.feature.memo.ui.Res
import io.github.taetae98coding.diary.feature.memo.ui.memo_place_picker_map_content_description
import io.github.taetae98coding.diary.feature.memo.ui.memo_place_picker_map_empty_description
import io.github.taetae98coding.diary.feature.memo.ui.memo_place_picker_map_empty_title
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun MemoPlacePickerMapBody(
    onEvent: (MemoPlacePickerEvent) -> Unit,
    mapState: DiaryMapState,
    modifier: Modifier = Modifier,
    uiStateProvider: () -> MemoPlaceInputUiState = { MemoPlaceInputUiState() },
    mapUiStateProvider: () -> MemoPlacePickerMapUiState = { MemoPlacePickerMapUiState() },
) {
    val mapContentDescription = stringResource(Res.string.memo_place_picker_map_content_description)

    Column(modifier = modifier) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(DiaryTheme.dimens.fixedMapHeight)
                    .semantics { contentDescription = mapContentDescription },
        ) {
            PlacePinMarkerEffect(
                mapState = mapState,
                placeListProvider = { mapUiStateProvider().placeList },
            )
            MemoPlacePickerMoveMapEffect(
                onEvent = onEvent,
                mapState = mapState,
            )
            DiaryMap(
                state = mapState,
                modifier = Modifier.fillMaxSize(),
                onPinClick = { id -> onEvent(toggleEvent(id = id, uiState = uiStateProvider())) },
            )
        }
        Spacer(modifier = Modifier.height(DiaryTheme.dimens.componentSpacing))
        MemoPlacePickerMapList(
            onEvent = onEvent,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(weight = 1F, fill = false)
                    .height(DiaryTheme.dimens.pickerListHeight),
            uiStateProvider = uiStateProvider,
            mapUiStateProvider = mapUiStateProvider,
        )
    }
}

@Composable
internal fun MemoPlacePickerMapList(
    onEvent: (MemoPlacePickerEvent) -> Unit,
    modifier: Modifier = Modifier,
    uiStateProvider: () -> MemoPlaceInputUiState = { MemoPlaceInputUiState() },
    mapUiStateProvider: () -> MemoPlacePickerMapUiState = { MemoPlacePickerMapUiState() },
) {
    val mapUiState = mapUiStateProvider()

    DiaryCrossfade(
        targetState = mapUiState.isLoaded && mapUiState.placeList.isEmpty(),
        modifier = modifier,
    ) { isEmpty ->
        if (isEmpty) {
            DiaryPickerEmptyBox(
                title = stringResource(Res.string.memo_place_picker_map_empty_title),
                description = stringResource(Res.string.memo_place_picker_map_empty_description),
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(
                    items = mapUiState.placeList,
                    key = { place -> place.id },
                ) { place ->
                    MemoPlacePickerRow(
                        onEvent = onEvent,
                        modifier = Modifier.fillMaxWidth(),
                        place = place,
                        isSelected = uiStateProvider().selectedPlaceList.any { selectedPlace -> selectedPlace.id == place.id },
                    )
                }
            }
        }
    }
}

private fun toggleEvent(
    id: Uuid,
    uiState: MemoPlaceInputUiState,
): MemoPlacePickerEvent =
    if (uiState.selectedPlaceList.any { place -> place.id == id }) {
        MemoPlacePickerEvent.Unselect(id = id)
    } else {
        MemoPlacePickerEvent.Select(id = id)
    }

private class MemoPlacePickerMapUiStatePreviewParameter : PreviewParameterProvider<MemoPlacePickerMapUiState> {
    override val values: Sequence<MemoPlacePickerMapUiState> =
        sequenceOf(
            MemoPlacePickerMapUiState(isLoaded = true),
            MemoPlacePickerMapUiState(
                isLoaded = true,
                placeList = listOf(previewPlace(title = "집", color = 0xFF3A7BD5, latitude = 37.5665, longitude = 126.9780)),
            ),
        )
}

@ComponentPreview
@Composable
private fun MemoPlacePickerMapBodyPreview(
    @PreviewParameter(MemoPlacePickerMapUiStatePreviewParameter::class) mapUiState: MemoPlacePickerMapUiState,
) {
    val selectedPlaceList: List<Place> = remember(mapUiState) { mapUiState.placeList }

    DiaryTheme {
        Surface {
            MemoPlacePickerMapBody(
                onEvent = {},
                mapState = rememberDiaryMapState(),
                uiStateProvider = { MemoPlaceInputUiState(isSelectedPlaceLoaded = true, selectedPlaceList = selectedPlaceList) },
                mapUiStateProvider = { mapUiState },
            )
        }
    }
}

@ComponentPreview
@Composable
private fun MemoPlacePickerMapListPreview(
    @PreviewParameter(MemoPlacePickerMapUiStatePreviewParameter::class) mapUiState: MemoPlacePickerMapUiState,
) {
    DiaryTheme {
        Surface {
            MemoPlacePickerMapList(
                onEvent = {},
                mapUiStateProvider = { mapUiState },
            )
        }
    }
}
