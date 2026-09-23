@file:OptIn(ExperimentalForeignApi::class)

package io.github.taetae98coding.diary.compose.map.naver

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import io.github.taetae98coding.diary.compose.map.DiaryMapCamera
import io.github.taetae98coding.diary.compose.map.isFinite
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import swiftPMImport.DiaryKmp.compose.compose.map.NMFCameraPosition
import swiftPMImport.DiaryKmp.compose.compose.map.NMFCameraUpdate
import swiftPMImport.DiaryKmp.compose.compose.map.NMFMapView
import swiftPMImport.DiaryKmp.compose.compose.map.NMGLatLng

@Composable
internal fun NaverMapMoveCameraEffect(
    mapView: NMFMapView? = null,
    moveCommand: Flow<DiaryMapCamera> = emptyFlow(),
) {
    LaunchedEffect(mapView, moveCommand) {
        if (mapView == null) return@LaunchedEffect

        moveCommand.collect { camera ->
            if (!camera.isFinite) return@collect

            mapView.moveCamera(
                NMFCameraUpdate.cameraUpdateWithScrollTo(
                    NMGLatLng.latLngWithLat(lat = camera.latitude, lng = camera.longitude),
                ),
            )
        }
    }
}

internal fun DiaryMapCamera.toCameraUpdate(): NMFCameraUpdate =
    NMFCameraUpdate.cameraUpdateWithPosition(
        NMFCameraPosition.cameraPosition(
            NMGLatLng.latLngWithLat(lat = latitude, lng = longitude),
            zoom = zoom,
        ),
    )
