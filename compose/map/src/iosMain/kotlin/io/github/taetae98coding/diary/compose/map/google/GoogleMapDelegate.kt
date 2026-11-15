@file:OptIn(ExperimentalForeignApi::class)

package io.github.taetae98coding.diary.compose.map.google

import io.github.taetae98coding.diary.compose.map.DiaryMapBounds
import io.github.taetae98coding.diary.compose.map.DiaryMapCamera
import io.github.taetae98coding.diary.compose.map.DiaryMapCoordinate
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.compose.map.visibleBoundsOf
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
    val center = position.target.toDiaryMapCoordinate()

    moveCamera(
        DiaryMapCamera(
            latitude = center.latitude,
            longitude = center.longitude,
            zoom = position.zoom.toDouble(),
            bounds = mapView.visibleBounds(center = center),
        ),
    )
}

private fun GMSMapView.visibleBounds(center: DiaryMapCoordinate): DiaryMapBounds =
    projection.visibleRegion().useContents {
        visibleBoundsOf(
            center = center,
            corners =
                listOf(
                    DiaryMapCoordinate(latitude = nearLeft.latitude, longitude = nearLeft.longitude),
                    DiaryMapCoordinate(latitude = nearRight.latitude, longitude = nearRight.longitude),
                    DiaryMapCoordinate(latitude = farLeft.latitude, longitude = farLeft.longitude),
                    DiaryMapCoordinate(latitude = farRight.latitude, longitude = farRight.longitude),
                ),
        )
    }

private fun CValue<CLLocationCoordinate2D>.toDiaryMapCoordinate(): DiaryMapCoordinate = useContents { DiaryMapCoordinate(latitude = latitude, longitude = longitude) }
