@file:OptIn(ExperimentalForeignApi::class)

package io.github.taetae98coding.diary.compose.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import io.github.taetae98coding.diary.compose.permission.rememberIsPermissionGranted
import io.github.taetae98coding.diary.core.permission.Permission
import kotlinx.cinterop.ExperimentalForeignApi
import swiftPMImport.DiaryKmp.compose.compose.map.NMFNaverMapView
import kotlin.uuid.Uuid

@Composable
internal actual fun NaverMap(
    modifier: Modifier,
    state: DiaryMapState,
    onSpotClick: ((DiaryMapCoordinate) -> Unit)?,
    onPinClick: ((Uuid) -> Unit)?,
) {
    val cameraDelegate = remember(state) { NaverMapCameraDelegate(state = state) }
    val spotTouchDelegate =
        remember(onSpotClick) {
            onSpotClick?.let { callback -> NaverMapTouchDelegate(onSpotClick = callback) }
        }
    var attachedView by remember(state) { mutableStateOf<NMFNaverMapView?>(null) }
    val attachedMapView = attachedView?.mapView
    val isLocationPermissionGranted = rememberIsPermissionGranted(Permission.LOCATION)

    NaverMapSpotMarkerEffect(
        mapView = attachedMapView,
        state = state,
    )
    NaverMapPinMarkersEffect(
        mapView = attachedMapView,
        state = state,
        onPinClick = onPinClick,
    )
    NaverMapMoveCameraEffect(
        mapView = attachedMapView,
        moveCommand = state.moveCommand,
    )
    NaverMapMyLocationEffect(
        view = attachedView,
        isLocationPermissionGranted = isLocationPermissionGranted,
    )

    UIKitView(
        factory = {
            NMFNaverMapView().apply {
                showCompass = true
                showScaleBar = true
                showZoomControls = true
                showIndoorLevelPicker = true

                mapView.apply {
                    indoorMapEnabled = true
                    logoInteractionEnabled = true
                    scrollGestureEnabled = true
                    zoomGestureEnabled = true
                    tiltGestureEnabled = true
                    rotateGestureEnabled = true
                    stopGestureEnabled = true

                    state.camera?.let { camera -> moveCamera(camera.toCameraUpdate()) }
                    addCameraDelegate(delegate = cameraDelegate)
                    if (spotTouchDelegate != null) {
                        touchDelegate = spotTouchDelegate
                    }
                }
            }
        },
        modifier = modifier,
        update = { view -> attachedView = view },
        onRelease = { view ->
            view.mapView.removeCameraDelegate(delegate = cameraDelegate)
            view.mapView.touchDelegate = null
            attachedView = null
        },
    )
}
