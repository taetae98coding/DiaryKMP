@file:OptIn(ExperimentalForeignApi::class)

package io.github.taetae98coding.diary.compose.map.google

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.text.rememberTextMeasurer
import io.github.taetae98coding.diary.compose.map.DiaryMapPin
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.compose.map.PinMarkerImage
import io.github.taetae98coding.diary.compose.map.isFinite
import io.github.taetae98coding.diary.compose.map.pinMarkerImageKey
import io.github.taetae98coding.diary.compose.map.provider.label
import io.github.taetae98coding.diary.compose.map.rememberDiaryMapState
import io.github.taetae98coding.diary.compose.map.rememberPinMarkerImageFactory
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGPointMake
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.UIKit.accessibilityLabel
import swiftPMImport.DiaryKmp.compose.compose.map.GMSMapView
import swiftPMImport.DiaryKmp.compose.compose.map.GMSMarker

private const val PIN_ANCHOR_X = 0.5

@Composable
internal fun GoogleMapPinMarkersEffect(
    density: Float,
    mapView: GMSMapView? = null,
    state: DiaryMapState = rememberDiaryMapState(),
    isPinSelectable: Boolean = false,
) {
    val pinMarkerImageFactory = rememberPinMarkerImageFactory()
    val markers = remember { mutableListOf<GMSMarker>() }
    val textMeasurer = rememberTextMeasurer()

    DisposableEffect(markers) {
        onDispose {
            markers.forEach { marker -> marker.map = null }
        }
    }

    LaunchedEffect(mapView, markers, state, isPinSelectable, density, pinMarkerImageFactory, textMeasurer) {
        snapshotFlow { state.pins }
            .collect { pins ->
                val finitePins = pins.filter { pin -> pin.isFinite }
                val imageByKey =
                    if (mapView == null) {
                        emptyMap()
                    } else {
                        pinMarkerImageFactory.create(pinList = finitePins, density = density, textMeasurer = textMeasurer)
                    }

                markers.forEach { marker -> marker.map = null }
                markers.clear()

                if (mapView == null) return@collect

                finitePins
                    .forEach { pin ->
                        markers +=
                            GMSMarker().apply {
                                setPosition(
                                    CLLocationCoordinate2DMake(
                                        latitude = pin.coordinate.latitude,
                                        longitude = pin.coordinate.longitude,
                                    ),
                                )
                                tappable = isPinSelectable
                                userData = pin.id.toString()
                                applyPinAppearance(pin = pin, markerImage = imageByKey[pin.pinMarkerImageKey])
                                map = mapView
                            }
                    }
            }
    }
}

private fun GMSMarker.applyPinAppearance(
    pin: DiaryMapPin,
    markerImage: PinMarkerImage?,
) {
    if (markerImage == null) return

    icon = markerImage.image
    groundAnchor = CGPointMake(PIN_ANCHOR_X, markerImage.anchorY)
    // 라벨을 이미지에 그려 넣었으므로 화면 읽기 프로그램에는 라벨 문구를 따로 알려 준다.
    accessibilityLabel = pin.label
}
