package io.github.taetae98coding.diary.compose.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import com.google.maps.android.compose.CameraPositionState

@Composable
internal fun GoogleMapShareCameraEffect(
    cameraPositionState: CameraPositionState,
    state: DiaryMapState = rememberDiaryMapState(),
) {
    LaunchedEffect(cameraPositionState, state) {
        snapshotFlow { cameraPositionState.position }
            .collect { position ->
                state.moveCamera(
                    DiaryMapCamera(
                        latitude = position.target.latitude,
                        longitude = position.target.longitude,
                        zoom = position.zoom.toDouble(),
                        bounds = cameraPositionState.visibleBoundsOrNull(),
                    ),
                )
            }
    }
}

private fun CameraPositionState.visibleBoundsOrNull(): DiaryMapBounds? =
    projection?.visibleRegion?.latLngBounds?.let { bounds ->
        DiaryMapBounds(
            south = bounds.southwest.latitude,
            north = bounds.northeast.latitude,
            west = bounds.southwest.longitude,
            east = bounds.northeast.longitude,
        )
    }
