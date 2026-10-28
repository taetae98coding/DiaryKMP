package io.github.taetae98coding.diary.feature.memo.ui.place

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Stable
internal class MemoPlacePickerState(
    initialViewMode: MemoPlacePickerViewMode = MemoPlacePickerViewMode.LIST,
) {
    var viewMode: MemoPlacePickerViewMode by mutableStateOf(initialViewMode)
        private set

    var isMapViewVisited: Boolean by mutableStateOf(false)
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
}

@Composable
internal fun rememberMemoPlacePickerState(): MemoPlacePickerState = remember { MemoPlacePickerState() }
