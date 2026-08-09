@file:OptIn(ExperimentalForeignApi::class)

package io.github.taetae98coding.diary.compose.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import swiftPMImport.DiaryKmp.compose.compose.map.GMSCameraPosition
import swiftPMImport.DiaryKmp.compose.compose.map.GMSMapView

@Composable
internal fun GoogleMapMoveCameraEffect(
    mapView: GMSMapView? = null,
    moveCommand: Flow<DiaryMapCamera> = emptyFlow(),
) {
    LaunchedEffect(mapView, moveCommand) {
        if (mapView == null) return@LaunchedEffect

        moveCommand.collect { camera ->
            if (!camera.isFinite) return@collect

            mapView.setCamera(
                GMSCameraPosition.cameraWithLatitude(
                    latitude = camera.latitude,
                    longitude = camera.longitude,
                    zoom = mapView.camera.zoom,
                ),
            )
        }
    }
}

internal fun DiaryMapCamera.toCameraPosition(): GMSCameraPosition =
    GMSCameraPosition.cameraWithLatitude(
        latitude = latitude,
        longitude = longitude,
        zoom = zoom.toFloat(),
    )
