@file:OptIn(ExperimentalForeignApi::class)

package io.github.taetae98coding.diary.compose.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.cinterop.ExperimentalForeignApi
import swiftPMImport.DiaryKmp.compose.compose.map.GMSMapView

@Composable
internal fun GoogleMapMyLocationEffect(
    mapView: GMSMapView? = null,
    isLocationPermissionGranted: Boolean = false,
) {
    LaunchedEffect(mapView, isLocationPermissionGranted) {
        if (mapView == null) return@LaunchedEffect

        // 현 위치 점을 켜지 않으면 지도가 현 위치를 알지 못해 현 위치 버튼을 눌러도 지도가 움직이지 않는다.
        mapView.myLocationEnabled = isLocationPermissionGranted
        mapView.settings.myLocationButton = isLocationPermissionGranted
    }
}
