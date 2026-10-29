package io.github.taetae98coding.diary.feature.memo.ui.place

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.compose.place.toCoordinate
import io.github.taetae98coding.diary.core.model.location.Coordinate

@Stable
internal class MemoPlacePickerState(
    initialViewMode: MemoPlacePickerViewMode = MemoPlacePickerViewMode.LIST,
    initialIsMapViewVisited: Boolean = false,
) {
    var viewMode: MemoPlacePickerViewMode by mutableStateOf(initialViewMode)
        private set

    var isMapViewVisited: Boolean by mutableStateOf(initialIsMapViewVisited)
        private set

    fun toggleViewMode() {
        viewMode =
            when (viewMode) {
                MemoPlacePickerViewMode.LIST -> MemoPlacePickerViewMode.MAP
                MemoPlacePickerViewMode.MAP -> MemoPlacePickerViewMode.LIST
            }

        if (viewMode == MemoPlacePickerViewMode.MAP) {
            isMapViewVisited = true
        }
    }

    fun shownViewMode(mapState: DiaryMapState?): MemoPlacePickerViewMode =
        if (mapState == null) {
            MemoPlacePickerViewMode.LIST
        } else {
            viewMode
        }

    fun addCoordinate(
        mapState: DiaryMapState?,
        cardCoordinate: Coordinate?,
    ): Coordinate? =
        when (shownViewMode(mapState = mapState)) {
            MemoPlacePickerViewMode.LIST -> cardCoordinate
            MemoPlacePickerViewMode.MAP -> mapState?.coordinate?.toCoordinate()
        }

    companion object {
        val Saver: Saver<MemoPlacePickerState, Any> =
            listSaver(
                save = { state -> listOf(state.viewMode.name, state.isMapViewVisited) },
                restore = { saved ->
                    MemoPlacePickerState(
                        initialViewMode = MemoPlacePickerViewMode.valueOf(saved[0] as String),
                        initialIsMapViewVisited = saved[1] as Boolean,
                    )
                },
            )
    }
}

@Composable
internal fun rememberMemoPlacePickerState(): MemoPlacePickerState = rememberSaveable(saver = MemoPlacePickerState.Saver) { MemoPlacePickerState() }
