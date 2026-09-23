package io.github.taetae98coding.diary.feature.place.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.github.taetae98coding.diary.feature.place.ui.home.viewmode.PlaceHomeViewMode

@Stable
internal class PlaceHomeScaffoldState(
    initialViewMode: PlaceHomeViewMode = PlaceHomeViewMode.MAP,
) {
    var viewMode: PlaceHomeViewMode by mutableStateOf(initialViewMode)
        private set

    fun toggleViewMode() {
        viewMode = viewMode.toggled()
    }

    companion object {
        val Saver: Saver<PlaceHomeScaffoldState, String> =
            Saver(
                save = { state -> state.viewMode.name },
                restore = { saved -> PlaceHomeScaffoldState(initialViewMode = PlaceHomeViewMode.valueOf(saved)) },
            )
    }
}

@Composable
internal fun rememberPlaceHomeScaffoldState(): PlaceHomeScaffoldState =
    rememberSaveable(saver = PlaceHomeScaffoldState.Saver) {
        PlaceHomeScaffoldState()
    }
