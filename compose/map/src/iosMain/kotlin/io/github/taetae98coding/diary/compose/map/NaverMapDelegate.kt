@file:OptIn(ExperimentalForeignApi::class)

package io.github.taetae98coding.diary.compose.map

import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGPoint
import platform.darwin.NSObject
import swiftPMImport.DiaryKmp.compose.compose.map.NMFMapView
import swiftPMImport.DiaryKmp.compose.compose.map.NMFMapViewCameraDelegateProtocol
import swiftPMImport.DiaryKmp.compose.compose.map.NMFMapViewTouchDelegateProtocol
import swiftPMImport.DiaryKmp.compose.compose.map.NMGLatLng

internal class NaverMapCameraDelegate(
    private val state: DiaryMapState,
) : NSObject(),
    NMFMapViewCameraDelegateProtocol {
    override fun mapViewCameraIdle(mapView: NMFMapView) {
        val position = mapView.cameraPosition
        val contentBounds = mapView.contentBounds

        state.moveCamera(
            DiaryMapCamera(
                latitude = position.target.lat,
                longitude = position.target.lng,
                zoom = position.zoom,
                bounds =
                    DiaryMapBounds(
                        south = contentBounds.southWestLat,
                        north = contentBounds.northEastLat,
                        west = contentBounds.southWestLng,
                        east = contentBounds.northEastLng,
                    ),
            ),
        )
    }
}

internal class NaverMapTouchDelegate(
    private val onSpotClick: (DiaryMapCoordinate) -> Unit,
) : NSObject(),
    NMFMapViewTouchDelegateProtocol {
    override fun mapView(
        mapView: NMFMapView,
        didTapMap: NMGLatLng,
        point: CValue<CGPoint>,
    ) {
        onSpotClick(DiaryMapCoordinate(latitude = didTapMap.lat, longitude = didTapMap.lng))
    }
}
