@file:OptIn(ExperimentalForeignApi::class)

package io.github.taetae98coding.diary.compose.map

import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreLocation.CLLocationCoordinate2D
import platform.darwin.NSObject
import swiftPMImport.DiaryKmp.compose.compose.map.GMSCameraPosition
import swiftPMImport.DiaryKmp.compose.compose.map.GMSMapView
import swiftPMImport.DiaryKmp.compose.compose.map.GMSMapViewDelegateProtocol
import swiftPMImport.DiaryKmp.compose.compose.map.GMSMarker
import kotlin.uuid.Uuid

internal class GoogleMapDelegate(
    private val state: DiaryMapState,
    private val onSpotClick: ((DiaryMapCoordinate) -> Unit)?,
    private val onPinClick: ((Uuid) -> Unit)?,
) : NSObject(),
    GMSMapViewDelegateProtocol {
    override fun mapView(
        mapView: GMSMapView,
        idleAtCameraPosition: GMSCameraPosition,
    ) {
        state.shareCamera(mapView = mapView, position = idleAtCameraPosition)
    }

    override fun mapView(
        mapView: GMSMapView,
        didTapAtCoordinate: CValue<CLLocationCoordinate2D>,
    ) {
        val onSpotClick = onSpotClick ?: return

        didTapAtCoordinate.useContents {
            onSpotClick(DiaryMapCoordinate(latitude = latitude, longitude = longitude))
        }
    }

    override fun mapView(
        mapView: GMSMapView,
        didTapPOIWithPlaceID: String,
        name: String,
        location: CValue<CLLocationCoordinate2D>,
    ) {
        val onSpotClick = onSpotClick ?: return

        location.useContents {
            onSpotClick(DiaryMapCoordinate(latitude = latitude, longitude = longitude))
        }
    }

    override fun mapView(
        mapView: GMSMapView,
        didTapMarker: GMSMarker,
    ): Boolean {
        (didTapMarker.userData as? String)
            ?.let { userData -> Uuid.parseOrNull(userData) }
            ?.let { id -> onPinClick?.invoke(id) }

        return true
    }
}

private fun DiaryMapState.shareCamera(
    mapView: GMSMapView,
    position: GMSCameraPosition,
) {
    val bounds = mapView.visibleBounds()

    position.target.useContents {
        moveCamera(
            DiaryMapCamera(
                latitude = latitude,
                longitude = longitude,
                zoom = position.zoom.toDouble(),
                bounds = bounds,
            ),
        )
    }
}

private fun GMSMapView.visibleBounds(): DiaryMapBounds =
    projection.visibleRegion().useContents {
        DiaryMapBounds(
            south = minOf(nearLeft.latitude, nearRight.latitude),
            north = maxOf(farLeft.latitude, farRight.latitude),
            west = minOf(nearLeft.longitude, farLeft.longitude),
            east = maxOf(nearRight.longitude, farRight.longitude),
        )
    }
