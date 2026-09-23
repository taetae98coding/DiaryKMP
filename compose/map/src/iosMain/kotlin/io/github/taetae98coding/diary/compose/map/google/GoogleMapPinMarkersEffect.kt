@file:OptIn(ExperimentalForeignApi::class)

package io.github.taetae98coding.diary.compose.map.google

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import io.github.taetae98coding.diary.compose.map.DiaryMapPin
import io.github.taetae98coding.diary.compose.map.DiaryMapPinMarkerDefaults
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.compose.map.createPinMarkerImage
import io.github.taetae98coding.diary.compose.map.isFinite
import io.github.taetae98coding.diary.compose.map.provider.label
import io.github.taetae98coding.diary.compose.map.rememberDiaryMapState
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGPointMake
import platform.CoreGraphics.CGRectMake
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.UIKit.NSTextAlignmentCenter
import platform.UIKit.UIColor
import platform.UIKit.UIFont
import platform.UIKit.UIImageView
import platform.UIKit.UILabel
import platform.UIKit.UIView
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
    val markers = remember { mutableListOf<GMSMarker>() }

    DisposableEffect(markers) {
        onDispose {
            markers.forEach { marker -> marker.map = null }
        }
    }

    LaunchedEffect(mapView, markers, state, isPinSelectable, density) {
        snapshotFlow { state.pins }
            .collect { pins ->
                markers.forEach { marker -> marker.map = null }
                markers.clear()

                if (mapView == null) return@collect

                pins
                    .filter { pin -> pin.isFinite }
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
                                applyPinAppearance(pin = pin, density = density)
                                map = mapView
                            }
                    }
            }
    }
}

private fun GMSMarker.applyPinAppearance(
    pin: DiaryMapPin,
    density: Float,
) {
    val image = createPinMarkerImage(color = pin.color, density = density) ?: return
    val (imageWidth, imageHeight) = image.size.useContents { width to height }
    val imageView = UIImageView(image = image)

    if (pin.label.isEmpty()) {
        iconView = imageView
        return
    }

    val label =
        UILabel().apply {
            text = pin.label
            font = UIFont.systemFontOfSize(DiaryMapPinMarkerDefaults.LABEL_FONT_SIZE_SP.toDouble())
            textColor = UIColor.blackColor
            backgroundColor = UIColor.whiteColor.colorWithAlphaComponent(DiaryMapPinMarkerDefaults.LABEL_BACKGROUND_ALPHA.toDouble())
            textAlignment = NSTextAlignmentCenter
            sizeToFit()
        }
    val (labelWidth, labelHeight) = label.frame.useContents { size.width to size.height }
    val width = maxOf(imageWidth, labelWidth)
    val height = imageHeight + labelHeight
    val container = UIView(frame = CGRectMake(0.0, 0.0, width, height))

    imageView.setFrame(CGRectMake((width - imageWidth) / 2, 0.0, imageWidth, imageHeight))
    label.setFrame(CGRectMake((width - labelWidth) / 2, imageHeight, labelWidth, labelHeight))
    container.addSubview(imageView)
    container.addSubview(label)

    iconView = container
    groundAnchor = CGPointMake(PIN_ANCHOR_X, imageHeight / height)
}
