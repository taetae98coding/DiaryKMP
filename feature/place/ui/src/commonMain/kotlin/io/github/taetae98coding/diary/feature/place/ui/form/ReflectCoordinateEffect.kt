@file:OptIn(FlowPreview::class)

package io.github.taetae98coding.diary.feature.place.ui.form

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import io.github.taetae98coding.diary.library.coroutines.flow.INPUT_IDLE_DELAY
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce

@Composable
internal fun ReflectCoordinateEffect(state: PlaceFormState = rememberPlaceAddFormState()) {
    val mapState = state.mapState

    LaunchedEffect(state, mapState) {
        snapshotFlow { state.spot }
            .debounce(INPUT_IDLE_DELAY)
            .collect { spot ->
                if (spot == mapState.spot) return@collect

                mapState.selectSpot(spot)
                spot?.let(mapState::moveTo)
            }
    }
}
