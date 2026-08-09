package io.github.taetae98coding.diary.compose.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import kotlin.uuid.Uuid

@Composable
internal actual fun NaverMap(
    modifier: Modifier,
    state: DiaryMapState,
    onSpotClick: ((DiaryMapCoordinate) -> Unit)?,
    onPinClick: ((Uuid) -> Unit)?,
) {
    val mapView = rememberNaverMapViewWithLifecycle(camera = state.camera)
    val client = rememberNaverMapClient(mapView = mapView)
    val isLocationPermissionGranted = rememberIsLocationPermissionGranted()

    NaverMapShareCameraEffect(
        client = client,
        state = state,
    )
    NaverMapSelectSpotEffect(
        client = client,
        onSpotClick = onSpotClick,
    )
    NaverMapSpotMarkerEffect(
        client = client,
        state = state,
    )
    NaverMapPinMarkersEffect(
        client = client,
        state = state,
        onPinClick = onPinClick,
    )
    NaverMapMoveCameraEffect(
        client = client,
        moveCommand = state.moveCommand,
    )
    NaverMapMyLocationEffect(
        client = client,
        isLocationPermissionGranted = isLocationPermissionGranted,
    )

    AndroidView(
        factory = { mapView },
        modifier = modifier,
    )
}
