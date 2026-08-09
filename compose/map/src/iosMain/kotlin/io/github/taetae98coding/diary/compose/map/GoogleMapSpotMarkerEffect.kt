@file:OptIn(ExperimentalForeignApi::class)

package io.github.taetae98coding.diary.compose.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreLocation.CLLocationCoordinate2DMake
import swiftPMImport.DiaryKmp.compose.compose.map.GMSMapView
import swiftPMImport.DiaryKmp.compose.compose.map.GMSMarker

@Composable
internal fun GoogleMapSpotMarkerEffect(
    mapView: GMSMapView? = null,
    state: DiaryMapState = rememberDiaryMapState(),
) {
    val marker = remember { GMSMarker() }

    DisposableEffect(marker) {
        onDispose {
            marker.map = null
        }
    }

    LaunchedEffect(mapView, marker, state) {
        snapshotFlow { state.spot }
            .collect { spot ->
                if (mapView == null || spot == null || !spot.isFinite) {
                    marker.map = null
                    return@collect
                }

                marker.setPosition(CLLocationCoordinate2DMake(latitude = spot.latitude, longitude = spot.longitude))
                marker.map = mapView
            }
    }
}
