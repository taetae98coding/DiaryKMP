package io.github.taetae98coding.diary.compose.map.google

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import com.google.maps.android.compose.CameraPositionState
import io.github.taetae98coding.diary.compose.map.DiaryMapBounds
import io.github.taetae98coding.diary.compose.map.DiaryMapCamera
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.compose.map.rememberDiaryMapState

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
