@file:OptIn(ExperimentalForeignApi::class)

package io.github.taetae98coding.diary.compose.map.naver

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.compose.map.isFinite
import io.github.taetae98coding.diary.compose.map.rememberDiaryMapState
import kotlinx.cinterop.ExperimentalForeignApi
import swiftPMImport.DiaryKmp.compose.compose.map.NMFMapView
import swiftPMImport.DiaryKmp.compose.compose.map.NMFMarker
import swiftPMImport.DiaryKmp.compose.compose.map.NMGLatLng

@Composable
internal fun NaverMapSpotMarkerEffect(
    mapView: NMFMapView? = null,
    state: DiaryMapState = rememberDiaryMapState(),
) {
    val marker = remember { NMFMarker() }

    DisposableEffect(marker) {
        onDispose {
            marker.mapView = null
        }
    }

    LaunchedEffect(mapView, marker, state) {
        snapshotFlow { state.spot }
            .collect { spot ->
                if (mapView == null || spot == null || !spot.isFinite) {
                    marker.mapView = null
                    return@collect
                }

                marker.position = NMGLatLng.latLngWithLat(lat = spot.latitude, lng = spot.longitude)
                marker.mapView = mapView
            }
    }
}
