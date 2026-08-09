@file:OptIn(ExperimentalForeignApi::class)

package io.github.taetae98coding.diary.compose.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import swiftPMImport.DiaryKmp.compose.compose.map.GMSMapView
import swiftPMImport.DiaryKmp.compose.compose.map.GMSMapViewDelegateProtocol
import kotlin.uuid.Uuid

@Composable
internal actual fun GoogleMap(
    modifier: Modifier,
    state: DiaryMapState,
    onSpotClick: ((DiaryMapCoordinate) -> Unit)?,
    onPinClick: ((Uuid) -> Unit)?,
) {
    val mapDelegate: GMSMapViewDelegateProtocol =
        remember(state, onSpotClick, onPinClick) {
            GoogleMapDelegate(
                state = state,
                onSpotClick = onSpotClick,
                onPinClick = onPinClick,
            )
        }
    var mapView by remember(state) { mutableStateOf<GMSMapView?>(null) }
    val isLocationPermissionGranted = rememberIsLocationPermissionGranted()

    GoogleMapSpotMarkerEffect(
        mapView = mapView,
        state = state,
    )
    GoogleMapPinMarkersEffect(
        mapView = mapView,
        state = state,
        isPinSelectable = onPinClick != null,
        density = LocalDensity.current.density,
    )
    GoogleMapMoveCameraEffect(
        mapView = mapView,
        moveCommand = state.moveCommand,
    )
    GoogleMapMyLocationEffect(
        mapView = mapView,
        isLocationPermissionGranted = isLocationPermissionGranted,
    )

    UIKitView(
        factory = {
            GMSMapView().apply {
                indoorEnabled = true

                settings.apply {
                    compassButton = true
                    indoorPicker = true
                    consumesGesturesInView = true
                    scrollGestures = true
                    zoomGestures = true
                    tiltGestures = true
                    rotateGestures = true
                    allowScrollGesturesDuringRotateOrZoom = true
                }

                state.camera?.let { camera -> setCamera(camera.toCameraPosition()) }
                setDelegate(mapDelegate)
            }
        },
        modifier = modifier,
        update = { view -> mapView = view },
        onRelease = { view ->
            view.setDelegate(null)
            mapView = null
        },
    )
}
