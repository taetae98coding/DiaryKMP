package io.github.taetae98coding.diary.compose.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import kotlin.uuid.Uuid
import com.google.maps.android.compose.GoogleMap as AndroidGoogleMap

@Composable
internal actual fun GoogleMap(
    modifier: Modifier,
    state: DiaryMapState,
    onSpotClick: ((DiaryMapCoordinate) -> Unit)?,
    onPinClick: ((Uuid) -> Unit)?,
) {
    val isLocationPermissionGranted = rememberIsLocationPermissionGranted()
    val properties = remember(isLocationPermissionGranted) { googleMapProperties(isLocationPermissionGranted) }
    val uiSettings = remember(isLocationPermissionGranted) { googleMapUiSettings(isLocationPermissionGranted) }
    val cameraPositionState =
        rememberCameraPositionState {
            state.camera?.let { camera ->
                position =
                    CameraPosition.fromLatLngZoom(
                        LatLng(camera.latitude, camera.longitude),
                        camera.zoom.toFloat(),
                    )
            }
        }

    GoogleMapShareCameraEffect(
        cameraPositionState = cameraPositionState,
        state = state,
    )
    GoogleMapMoveCameraEffect(
        cameraPositionState = cameraPositionState,
        moveCommand = state.moveCommand,
    )

    AndroidGoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = properties,
        uiSettings = uiSettings,
        onMapClick = remember(onSpotClick) { onSpotClick?.toMapClickListener() },
        onPOIClick = remember(onSpotClick) { onSpotClick?.toPoiClickListener() },
    ) {
        state.spot
            ?.takeIf { spot -> spot.isFinite }
            ?.let { spot -> GoogleMapSpotMarker(spot = spot) }
        state.pins
            .filter { pin -> pin.isFinite }
            .forEach { pin ->
                GoogleMapPinMarker(
                    pin = pin,
                    onPinClick = onPinClick,
                )
            }
    }
}

private fun googleMapProperties(isLocationPermissionGranted: Boolean): MapProperties =
    MapProperties(
        isIndoorEnabled = true,
        isMyLocationEnabled = isLocationPermissionGranted,
    )

private fun googleMapUiSettings(isLocationPermissionGranted: Boolean): MapUiSettings =
    MapUiSettings(
        compassEnabled = true,
        indoorLevelPickerEnabled = true,
        mapToolbarEnabled = true,
        myLocationButtonEnabled = isLocationPermissionGranted,
        rotationGesturesEnabled = true,
        scrollGesturesEnabled = true,
        scrollGesturesEnabledDuringRotateOrZoom = true,
        tiltGesturesEnabled = true,
        zoomControlsEnabled = true,
        zoomGesturesEnabled = true,
    )

private fun ((DiaryMapCoordinate) -> Unit).toMapClickListener(): (LatLng) -> Unit = { latLng -> this(latLng.toDiaryMapCoordinate()) }

private fun ((DiaryMapCoordinate) -> Unit).toPoiClickListener(): (PointOfInterest) -> Unit = { poi -> this(poi.latLng.toDiaryMapCoordinate()) }

private fun LatLng.toDiaryMapCoordinate(): DiaryMapCoordinate = DiaryMapCoordinate(latitude = latitude, longitude = longitude)
