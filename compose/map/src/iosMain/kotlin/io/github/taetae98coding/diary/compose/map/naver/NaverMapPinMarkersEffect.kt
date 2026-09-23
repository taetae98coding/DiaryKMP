@file:OptIn(ExperimentalForeignApi::class)

package io.github.taetae98coding.diary.compose.map.naver

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.compose.map.isFinite
import io.github.taetae98coding.diary.compose.map.provider.label
import io.github.taetae98coding.diary.compose.map.rememberDiaryMapState
import io.github.taetae98coding.diary.compose.map.toUIColor
import kotlinx.cinterop.ExperimentalForeignApi
import swiftPMImport.DiaryKmp.compose.compose.map.NMFMapView
import swiftPMImport.DiaryKmp.compose.compose.map.NMFMarker
import swiftPMImport.DiaryKmp.compose.compose.map.NMF_MARKER_IMAGE_BLACK
import swiftPMImport.DiaryKmp.compose.compose.map.NMGLatLng
import kotlin.uuid.Uuid

@Composable
internal fun NaverMapPinMarkersEffect(
    mapView: NMFMapView? = null,
    state: DiaryMapState = rememberDiaryMapState(),
    onPinClick: ((Uuid) -> Unit)? = null,
) {
    val markers = remember { mutableListOf<NMFMarker>() }

    DisposableEffect(markers) {
        onDispose {
            markers.forEach { marker -> marker.mapView = null }
        }
    }

    LaunchedEffect(mapView, markers, state, onPinClick) {
        snapshotFlow { state.pins }
            .collect { pins ->
                markers.forEach { marker -> marker.mapView = null }
                markers.clear()

                if (mapView == null) return@collect

                pins
                    .filter { pin -> pin.isFinite }
                    .forEach { pin ->
                        markers +=
                            NMFMarker().apply {
                                position = NMGLatLng.latLngWithLat(lat = pin.coordinate.latitude, lng = pin.coordinate.longitude)
                                iconImage = NMF_MARKER_IMAGE_BLACK
                                iconTintColor = pin.color.toUIColor()
                                captionText = pin.label
                                if (onPinClick != null) {
                                    touchHandler = { _ ->
                                        onPinClick(pin.id)
                                        true
                                    }
                                }
                                this.mapView = mapView
                            }
                    }
            }
    }
}
